package cn.ykf.service;

import cn.ykf.entity.Result;
import cn.ykf.model.TradeCoupon;

/**
 * 优惠券业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
public interface CouponService {
    /**
     * 根据id查询
     *
     * @param couponId 优惠券id
     * @return 对应优惠券
     */
    TradeCoupon get(Long couponId);

    /**
     * 更新优惠券状态
     *
     * @param coupon 待更新优惠券
     * @return Result
     */
    Result updateCouponStatus(TradeCoupon coupon);

    /**
     * 处理取消订单消息
     *
     * @param tags          消息tag
     * @param msgId         消息id
     * @param keys          消息key
     * @param body          消息内容
     * @param consumerGroup 消费组名
     */
    void handlerCancelOrderMsg(String tags, String msgId, String keys, String body, String consumerGroup);
}
