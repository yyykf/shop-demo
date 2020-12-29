package cn.ykf.service;

import cn.ykf.entity.Result;
import cn.ykf.model.TradeGoods;
import cn.ykf.model.TradeGoodsNumberLog;

/**
 * 商品业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
public interface GoodsService {
    /**
     * 根据id查询
     *
     * @param goodsId 商品id
     * @return 对应商品
     */
    TradeGoods get(Long goodsId);

    /**
     * 扣减库存
     *
     * @param goodsNumberLog 包含待扣减库存信息
     * @return Result
     */
    Result reduceGoodsStock(TradeGoodsNumberLog goodsNumberLog);

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
