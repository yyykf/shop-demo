package cn.ykf.service;

import cn.ykf.entity.Result;
import cn.ykf.model.TradeUser;
import cn.ykf.model.TradeUserMoneyLog;

/**
 * 用户业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
public interface UserService {
    /**
     * 根据id查询
     *
     * @param userId 用户id
     * @return 对应用户
     */
    TradeUser get(Long userId);

    /**
     * 更新余额
     *
     * @param userMoneyLog 包含待更新余额信息
     * @return Result
     */
    Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog);

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
