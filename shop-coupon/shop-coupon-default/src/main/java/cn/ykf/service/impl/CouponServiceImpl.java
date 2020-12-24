package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeCouponMapper;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeCoupon;
import cn.ykf.service.CouponService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 优惠券业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
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
}
