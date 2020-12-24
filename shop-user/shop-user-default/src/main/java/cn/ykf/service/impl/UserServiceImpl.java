package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeUserMapper;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeUser;
import cn.ykf.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 用户业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@DubboService
public class UserServiceImpl implements UserService {

    @Resource
    private TradeUserMapper userMapper;

    @Override
    public TradeUser get(Long userId) {
        if (userId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return userMapper.selectByPrimaryKey(userId);
    }
}
