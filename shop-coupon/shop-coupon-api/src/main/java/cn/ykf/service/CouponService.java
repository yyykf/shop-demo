package cn.ykf.service;

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
}
