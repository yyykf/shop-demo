package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeCouponMapper;
import cn.ykf.entity.CancelOrderMsg;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeCoupon;
import cn.ykf.service.CouponService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 优惠券业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@Slf4j
@DubboService
public class CouponServiceImpl implements CouponService {

    @Resource
    private TradeCouponMapper couponMapper;


    @Override
    public TradeCoupon get(Long couponId) {
        if (couponId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return couponMapper.selectByPrimaryKey(couponId);
    }

    @Override
    public Result updateCouponStatus(TradeCoupon coupon) {
        if (coupon == null || coupon.getIsUsed() == null || coupon.getCouponId() == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        try {
            return couponMapper.updateByPrimaryKeySelective(coupon) > 0
                    ? Result.of(ShopCode.SHOP_SUCCESS)
                    : Result.of(ShopCode.SHOP_FAIL);
        } catch (Exception e) {
            log.error("更新优惠券失败：{}", coupon);
        }

        return Result.of(ShopCode.SHOP_FAIL);
    }

    @Override
    public void handlerCancelOrderMsg(String tags, String msgId, String keys, String body, String consumerGroup) {
        // todo 方法能不能抽取到common？但是会导致common引入mybatis
        if (StringUtils.isAnyBlank(tags, keys, consumerGroup, msgId, body)) {
            log.error("消息必需参数不完整, tags: {}, keys: {}, consumerGroup: {}, msgId: {}, body: {}", tags, keys, consumerGroup, msgId, body);
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        CancelOrderMsg cancelOrderMsg = JSON.parseObject(body, CancelOrderMsg.class);
        if (cancelOrderMsg == null) {
            log.error("消息体反序列化为空, {}", body);
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }

        try {
            // todo 保存日志
            // 回退库存
            this.returnCoupon(cancelOrderMsg.getCouponId());

            // todo 更新消费日志
            log.info("优惠券 {} 返还成功", cancelOrderMsg.getCouponId());
        } catch (Exception e) {
            // todo 消费失败，修改消费日志状态
            log.error("优惠券 {} 返还失败，等待重试", cancelOrderMsg.getCouponId());
            // 让MQ重新投递
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }
    }

    /**
     * 返还优惠券
     *
     * @param couponId 待返还优惠券id
     */
    private void returnCoupon(Long couponId) {
        TradeCoupon coupon = this.get(couponId);
        if (coupon == null) {
            log.info("优惠券 {} 不存在", couponId);
            return;
        }

        coupon.setOrderId(null);
        coupon.setUsedTime(null);
        coupon.setIsUsed(ShopCode.SHOP_COUPON_UNUSED.getCode());
        couponMapper.updateByPrimaryKey(coupon);
    }
}
