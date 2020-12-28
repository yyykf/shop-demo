package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeUserMapper;
import cn.ykf.dao.TradeUserMoneyLogMapper;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeUser;
import cn.ykf.model.TradeUserMoneyLog;
import cn.ykf.model.TradeUserMoneyLogExample;
import cn.ykf.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private TradeUserMoneyLogMapper userMoneyLogMapper;

    @Override
    public TradeUser get(Long userId) {
        if (userId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog) {
        if (userMoneyLog == null
                || userMoneyLog.getUserId() == null
                || userMoneyLog.getOrderId() == null
                || userMoneyLog.getUseMoney() == null
                || BigDecimal.ZERO.compareTo(userMoneyLog.getUseMoney()) >= 0) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        TradeUser user = userMapper.selectByPrimaryKey(userMoneyLog.getUserId());
        if (user == null) {
            BusinessException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }

        // 是否有过对应操作记录
        TradeUserMoneyLogExample example = new TradeUserMoneyLogExample();
        example.createCriteria()
                .andUserIdEqualTo(userMoneyLog.getUserId())
                .andOrderIdEqualTo(userMoneyLog.getOrderId());
        List<TradeUserMoneyLog> logs = userMoneyLogMapper.selectByExample(example);

        // 付款操作
        if (Objects.equals(ShopCode.SHOP_USER_MONEY_PAID.getCode(), userMoneyLog.getMoneyLogType())) {
            // 已经付款过或者退款过
            if (logs.size() > 0) {
                BusinessException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }

            user.setUserMoney(BigDecimal.valueOf(user.getUserMoney()).subtract(userMoneyLog.getUseMoney()).longValue());
            userMapper.updateByPrimaryKeySelective(user);
        }
        // 退款操作
        if (Objects.equals(ShopCode.SHOP_USER_MONEY_REFUND.getCode(), userMoneyLog.getMoneyLogType())) {
            // 未付款
            if (logs.isEmpty()) {
                BusinessException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
            }
            // 已经退过款
            if (logs.stream().anyMatch(log -> Objects.equals(ShopCode.SHOP_USER_MONEY_REFUND.getCode(), log.getMoneyLogType()))) {
                BusinessException.cast(ShopCode.SHOP_USER_MONEY_REFUND_ALREADY);
            }

            user.setUserMoney(BigDecimal.valueOf(user.getUserMoney()).add(userMoneyLog.getUseMoney()).longValue());
            userMapper.updateByPrimaryKeySelective(user);
        }

        userMoneyLog.setCreateTime(new Date());
        userMoneyLogMapper.insert(userMoneyLog);

        return Result.of(ShopCode.SHOP_SUCCESS);
    }
}
