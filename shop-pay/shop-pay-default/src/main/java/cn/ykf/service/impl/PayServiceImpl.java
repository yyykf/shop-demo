package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradePayMapper;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradePay;
import cn.ykf.model.TradePayExample;
import cn.ykf.model.vo.PayVo;
import cn.ykf.service.PayService;
import cn.ykf.util.IdHelper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 支付业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/30
 */
@Slf4j
@DubboService
public class PayServiceImpl implements PayService {

    @Resource
    private TradePayMapper payMapper;

    @Resource
    private RocketMQTemplate mqTemplate;

    @Value("${pay.topic}")
    private String topic;

    @Value("${pay.tag}")
    private String tag;

    @Override
    public Result createPayment(TradePay tradePay) {
        if (tradePay == null || tradePay.getOrderId() == null || tradePay.getPayAmount() == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        // 查询是否已经支付过
        TradePayExample condition = new TradePayExample();
        condition.createCriteria()
                .andOrderIdEqualTo(tradePay.getOrderId())
                .andIsPaidEqualTo(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        int count = payMapper.countByExample(condition);
        if (count > 0) {
            log.warn("订单 {} 已经支付过", tradePay.getOrderId());
            BusinessException.cast(ShopCode.SHOP_PAYMENT_IS_PAID);
        }

        tradePay.setPayId(IdHelper.getNewId(TradePayMapper.class));
        tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());

        return payMapper.insert(tradePay) > 0 ? Result.of(ShopCode.SHOP_SUCCESS) : Result.of(ShopCode.SHOP_FAIL);
    }

    @Override
    public Result callbackPayment(TradePay tradePay) {
        if (tradePay == null
                || tradePay.getPayId() == null
                || tradePay.getIsPaid() == null
                || tradePay.getOrderId() == null) {
            log.error("参数不完整, {}", JSON.toJSONString(tradePay));
            return Result.of(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        if (!Objects.equals(tradePay.getIsPaid(), ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode())) {
            log.error("订单未支付成功, 支付状态: {}", tradePay.getIsPaid());
            return Result.of(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
        }

        // 发送事务消息
        TransactionSendResult result = mqTemplate.sendMessageInTransaction(String.format("%s:%s",
                topic, tag),
                MessageBuilder.withPayload(PayVo.of(tradePay.getPayId(), tradePay.getOrderId())).build(),
                tradePay.getPayId());

        log.info("事务消息结果：{}, {}", result, result.getLocalTransactionState());

        if (LocalTransactionState.COMMIT_MESSAGE != result.getLocalTransactionState()) {
            return Result.of(ShopCode.SHOP_FAIL);
        }

        return Result.of(ShopCode.SHOP_SUCCESS);
    }

    @Override
    public Result updatePaymentToSuccess(Long payId) {
        if (payId == null) {
            log.error("支付订单id为空");
            return Result.of(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        TradePay tradePay = payMapper.selectByPrimaryKey(payId);
        if (tradePay == null) {
            log.error("{} 对应订单为空", payId);
            return Result.of(ShopCode.SHOP_PAYMENT_NOT_FOUND);
        }

        if (Objects.equals(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode(), tradePay.getIsPaid())) {
            log.info("{} 对应订单已经支付过，无需再次支付", payId);
            return Result.of(ShopCode.SHOP_PAYMENT_IS_PAID);
        }

        // 更新支付订单状态
        TradePayExample condition = new TradePayExample();
        condition.createCriteria().andPayIdEqualTo(payId).andIsPaidEqualTo(tradePay.getIsPaid());

        tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        int result = payMapper.updateByExampleSelective(tradePay, condition);

        if (result == 0) {
            log.info("{} 支付订单已经被并发修改", payId);
            return Result.of(ShopCode.SHOP_FAIL);
        }

        return Result.of(ShopCode.SHOP_SUCCESS);
    }

    @Override
    public boolean checkPayIsPaid(Long payId) {
        if (payId == null) {
            log.error("支付订单id为空");
            return false;
        }

        TradePay tradePay = payMapper.selectByPrimaryKey(payId);
        if (tradePay == null) {
            log.error("{} 对应订单为空", payId);
            return false;
        }

        return Objects.equals(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode(), tradePay.getIsPaid());
    }
}