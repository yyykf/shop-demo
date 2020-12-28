package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeGoodsMapper;
import cn.ykf.dao.TradeGoodsNumberLogMapper;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeGoods;
import cn.ykf.model.TradeGoodsNumberLog;
import cn.ykf.service.GoodsService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 商品业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@DubboService
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private TradeGoodsMapper goodsMapper;

    @Resource
    private TradeGoodsNumberLogMapper logMapper;

    @Override
    public TradeGoods get(Long goodsId) {
        if (goodsId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return goodsMapper.selectByPrimaryKey(goodsId);
    }

    @Override
    public Result reduceGoodsStock(TradeGoodsNumberLog goodsNumberLog) {
        // 校验
        // fixme RPC调用时，直接抛出异常不太好，应该封装为Result返回
        if (goodsNumberLog == null ||
                goodsNumberLog.getGoodsId() == null ||
                goodsNumberLog.getOrderId() == null ||
                goodsNumberLog.getGoodsNumber() == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        // 先查商品
        TradeGoods goods = goodsMapper.selectByPrimaryKey(goodsNumberLog.getGoodsId());
        if (goods == null) {
            BusinessException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }

        // 如果此时的库存不足，那么都不用通过乐观锁来执行sql了
        Integer availableStock = goods.getGoodsNumber();
        Integer needStock = goodsNumberLog.getGoodsNumber();
        if (availableStock < needStock) {
            BusinessException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }

        // 更新库存
        int result = goodsMapper.updateStock(availableStock - needStock, availableStock, goodsNumberLog.getGoodsId());
        // 更新失败说明可用库存被并发修改了
        if (result == 0) {
            BusinessException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }

        // 记录日志，扣减，数量应该为负数
        goods.setGoodsNumber(-(goodsNumberLog.getGoodsNumber()));
        goodsNumberLog.setLogTime(new Date());
        logMapper.insert(goodsNumberLog);

        return Result.of(ShopCode.SHOP_SUCCESS);
    }
}
