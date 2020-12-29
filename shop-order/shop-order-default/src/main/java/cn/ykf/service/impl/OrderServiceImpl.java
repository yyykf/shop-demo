package cn.ykf.service.impl;

import cn.ykf.config.OrderMsgProperties;
import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeOrderMapper;
import cn.ykf.entity.CancelOrderMsg;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeCoupon;
import cn.ykf.model.TradeGoods;
import cn.ykf.model.TradeGoodsNumberLog;
import cn.ykf.model.TradeOrder;
import cn.ykf.model.TradeUser;
import cn.ykf.model.TradeUserMoneyLog;
import cn.ykf.service.CouponService;
import cn.ykf.service.GoodsService;
import cn.ykf.service.OrderService;
import cn.ykf.service.UserService;
import cn.ykf.util.IdHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 订单业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@Slf4j
@DubboService
public class OrderServiceImpl implements OrderService {

    @DubboReference(check = false)
    private CouponService couponService;

    @DubboReference(check = false)
    private GoodsService goodsService;

    @DubboReference(check = false)
    private UserService userService;

    @Resource
    private TradeOrderMapper orderMapper;

    @Resource
    private OrderMsgProperties orderMsgProperties;

    @Resource
    private RocketMQTemplate mqTemplate;


    @Override
    public Result confirmOrder(TradeOrder order) {
        // 校验订单
        this.checkOrder(order);
        // 生成预订单
        Long orderId = this.savePreOrder(order);
        try {
            // 扣减库存
            this.reduceGoodsStock(order);
            // 扣减优惠券
            this.updateCouponStatus(order);
            // 使用余额
            this.reduceMoneyPaid(order);
            // 确认订单
            this.updateOrderStatus(order);

            log.info("订单:[" + orderId + "]确认成功");
        } catch (Exception e) {
            log.error("确认订单失败，准备回滚", e);

            CancelOrderMsg cancelMsg = CancelOrderMsg.of(orderId, order.getCouponId(), order.getUserId(), order.getMoneyPaid(), order.getGoodsId(), order.getGoodsNumber());
            this.sendCancelOrderMsg(cancelMsg);

            return Result.of(ShopCode.SHOP_FAIL);
        }
        return Result.of(ShopCode.SHOP_SUCCESS);
    }

    /**
     * 发送取消订单消息
     *
     * @param cancelMsg 待发送的取消订单信息
     */
    private void sendCancelOrderMsg(CancelOrderMsg cancelMsg) {
        log.info("发送取消订单消息");
        SendResult sendResult = mqTemplate.syncSend(this.getCancelOrderMsgDestination(), MessageBuilder.withPayload(cancelMsg).build());
        log.info("发送结果：{}", sendResult);
    }

    /**
     * 获取取消订单消息的目的地，格式：Topic:Tag
     *
     * @return 取消订单消息目的地
     */
    private String getCancelOrderMsgDestination() {
        return String.join(":", orderMsgProperties.getTopic(), orderMsgProperties.getTag().getCancel());
    }

    /**
     * 更新订单为已确认
     *
     * @param order 待更新订单
     */
    private void updateOrderStatus(TradeOrder order) {
        order.setOrderStatus(ShopCode.SHOP_ORDER_CONFIRM.getCode());
        order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
        order.setConfirmTime(new Date());

        int result = orderMapper.updateByPrimaryKeySelective(order);
        if (result == 0) {
            BusinessException.cast(ShopCode.SHOP_ORDER_CONFIRM_FAIL);
        }
    }

    /**
     * 根据订单中金额扣减用户余额
     *
     * @param order 订单
     */
    private void reduceMoneyPaid(TradeOrder order) {
        if (order == null || order.getMoneyPaid() == null || BigDecimal.ZERO.compareTo(order.getMoneyPaid()) > 0) {
            // 该订单未使用余额或
            return;
        }

        TradeUserMoneyLog userMoneyLog = new TradeUserMoneyLog(order.getUserId(), order.getOrderId(),
                ShopCode.SHOP_USER_MONEY_PAID.getCode(), order.getMoneyPaid());

        // 扣减余额
        Result result = userService.updateMoneyPaid(userMoneyLog);
        if (Objects.equals(ShopCode.SHOP_FAIL.getSuccess(), result.getSuccess())) {
            BusinessException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
        }
        log.info("订单:" + order.getOrderId() + ",扣减余额成功");
    }

    /**
     * 更新优惠券状态为已使用
     *
     * @param order 订单
     */
    private void updateCouponStatus(TradeOrder order) {
        if (order == null || StringUtils.isEmpty(order.getCouponId())) {
            return;
        }

        TradeCoupon coupon = couponService.get(order.getCouponId());
        if (coupon == null) {
            BusinessException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
        }
        if (Objects.equals(ShopCode.SHOP_COUPON_USED.getCode(), coupon.getIsUsed())) {
            BusinessException.cast(ShopCode.SHOP_COUPON_USED);
        }

        coupon.setIsUsed(ShopCode.SHOP_COUPON_USED.getCode());
        coupon.setUsedTime(new Date());
        coupon.setOrderId(order.getOrderId());

        // 更新优惠券
        Result result = couponService.updateCouponStatus(coupon);
        if (Objects.equals(ShopCode.SHOP_FAIL.getSuccess(), result.getSuccess())) {
            BusinessException.cast(ShopCode.SHOP_COUPON_USE_FAIL);
        }

        log.info("订单:[" + order.getOrderId() + "]使用扣减优惠券[" + coupon.getCouponPrice() + "元]成功");
    }

    /**
     * 扣减库存
     *
     * @param order 包含待操作商品的订单信息
     */
    private void reduceGoodsStock(TradeOrder order) {
        TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog(order.getGoodsId(), order.getOrderId(), order.getGoodsNumber());
        Result result = goodsService.reduceGoodsStock(goodsNumberLog);

        if (ShopCode.SHOP_FAIL.getSuccess().equals(result.getSuccess())) {
            BusinessException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }

        log.info("订单:[" + order.getOrderId() + "]扣减库存[" + order.getGoodsNumber() + "个]成功");
    }

    /**
     * 校验订单，校验不通过时抛出自定义异常
     *
     * @param order 待校验订单
     */
    private void checkOrder(TradeOrder order) {
        if (order == null) {
            BusinessException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        // 商品是否存在
        TradeGoods goods = goodsService.get(order.getGoodsId());
        if (goods == null) {
            BusinessException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        // 用户是否存在
        TradeUser user = userService.get(order.getUserId());
        if (user == null) {
            BusinessException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }
        // 单价是否合法
        if (order.getGoodsPrice().compareTo(goods.getGoodsPrice()) != 0) {
            BusinessException.cast(ShopCode.SHOP_ORDER_TOP_PRICE_INVALID);
        }
        // 数量是否合法
        if (order.getGoodsNumber() > goods.getGoodsNumber()) {
            // todo 校验时先检查后执行，可能有并发问题，减库存时应该使用乐观锁，防止超卖
            BusinessException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }

        log.info("订单校验成功");
    }

    /**
     * 核算运费
     *
     * @param orderAmount 订单金额
     * @return 运费金额
     */
    private BigDecimal calculateShippingFee(BigDecimal orderAmount) {
        if (orderAmount == null) {
            BusinessException.cast(ShopCode.SHOP_ORDER_TOP_PRICE_INVALID);
        }

        if (BigDecimal.valueOf(100).compareTo(orderAmount) <= 0) {
            return BigDecimal.ZERO;
        }

        // 订单金额不满100元需要10元运费
        return BigDecimal.TEN;
    }

    /**
     * 生成预订单，保存到数据库
     *
     * @param order 待生成的订单
     * @return 订单id
     */
    private Long savePreOrder(TradeOrder order) {
        // 设置订单状态为不可见
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());
        // 订单ID
        order.setOrderId(IdHelper.getNewId(orderMapper.getClass()));
        // 运费是否正确
        BigDecimal shippingFee = this.calculateShippingFee(order.getOrderAmount());
        if (order.getShippingFee().compareTo(shippingFee) != 0) {
            BusinessException.cast(ShopCode.SHOP_ORDER_EXPRESS_FEE_INVALID);
        }
        // 订单总价格是否正确
        BigDecimal orderAmount = order.getGoodsPrice().multiply(BigDecimal.valueOf(order.getGoodsNumber()));
        orderAmount = orderAmount.add(shippingFee);
        if (orderAmount.compareTo(order.getOrderAmount()) != 0) {
            BusinessException.cast(ShopCode.SHOP_ORDER_TOP_PRICE_INVALID);
        }

        // 判断优惠券信息是否合法
        Long couponId = order.getCouponId();
        if (couponId != null) {
            TradeCoupon coupon = couponService.get(couponId);
            // 优惠券不存在
            if (coupon == null) {
                BusinessException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
            }
            // 优惠券已经使用
            if (Objects.equals(ShopCode.SHOP_COUPON_USED.getCode(), coupon.getIsUsed())) {
                BusinessException.cast(ShopCode.SHOP_COUPON_INVALID);
            }
            order.setCouponPaid(coupon.getCouponPrice());
        } else {
            order.setCouponPaid(BigDecimal.ZERO);
        }

        // 判断余额是否正确
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null) {
            // 比较余额是否大于0
            int result = BigDecimal.ZERO.compareTo(order.getMoneyPaid());
            // 余额小于0
            if (result > 0) {
                BusinessException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            }
            // 余额大于0
            if (result < 0) {
                // 查询用户信息
                // todo 先检查后执行，一样会有并发问题，用户减余额应该乐观锁
                TradeUser user = userService.get(order.getUserId());
                if (user == null) {
                    BusinessException.cast(ShopCode.SHOP_USER_NO_EXIST);
                }
                // 比较余额是否大于用户账户余额
                if ((moneyPaid.compareTo(BigDecimal.valueOf(user.getUserMoney())) > 0)) {
                    BusinessException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
                order.setMoneyPaid(order.getMoneyPaid());
            }
        } else {
            order.setMoneyPaid(BigDecimal.ZERO);
        }

        // 计算订单支付总价，减去优惠卷金额和已支付余额
        order.setPayAmount(orderAmount.subtract(order.getCouponPaid()).subtract(order.getMoneyPaid()));
        // 设置订单添加时间
        order.setAddTime(new Date());

        // 保存预订单
        if (ShopCode.SHOP_SUCCESS.getCode() != orderMapper.insert(order)) {
            BusinessException.cast(ShopCode.SHOP_ORDER_SAVE_ERROR);
        }
        log.info("订单:[" + order.getOrderId() + "]预订单生成成功");
        return order.getOrderId();
    }

}
