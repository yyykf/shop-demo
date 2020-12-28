package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeCouponMapper;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeCoupon;
import cn.ykf.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.StringUtils;

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
        if (coupon == null || coupon.getIsUsed() == null || StringUtils.isEmpty(coupon.getCouponId())) {
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
}
