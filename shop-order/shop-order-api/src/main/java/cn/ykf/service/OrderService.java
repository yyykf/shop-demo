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
}
