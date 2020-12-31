package cn.ykf.service;

import cn.ykf.entity.Result;
import cn.ykf.model.TradeOrder;

/**
 * 订单业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
public interface OrderService {

    /**
     * 确认订单
     *
     * @param order 待确认订单
     * @return Order
     */
    Result confirmOrder(TradeOrder order);

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

    /**
     * 更新订单为已支付
     *
     * @param orderId 订单id
     */
    void updateOrderToIsPaid(Long orderId);
}
