package cn.ykf.dao;

import cn.ykf.model.TradeGoods;
import cn.ykf.model.TradeGoodsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TradeGoodsMapper {
    int countByExample(TradeGoodsExample example);

    int deleteByExample(TradeGoodsExample example);

    int deleteByPrimaryKey(Long goodsId);

    int insert(TradeGoods record);

    int insertSelective(TradeGoods record);

    List<TradeGoods> selectByExample(TradeGoodsExample example);

    TradeGoods selectByPrimaryKey(Long goodsId);

    int updateByExampleSelective(@Param("record") TradeGoods record, @Param("example") TradeGoodsExample example);

    int updateByExample(@Param("record") TradeGoods record, @Param("example") TradeGoodsExample example);

    int updateByPrimaryKeySelective(TradeGoods record);

    int updateByPrimaryKey(TradeGoods record);

    /**
     * 更新库存，考虑并发、幂等
     *
     * @param remainingStock 新库存
     * @param availableStock 原库存
     * @param goodsId        货物id
     * @return 成功 - {@code 1}，失败 - {@code 0}
     */
    int updateStock(@Param("newStock") int remainingStock, @Param("oldStock") Integer availableStock, @Param("goodsId") Long goodsId);
}