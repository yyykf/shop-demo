package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradeGoodsMapper;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradeGoods;
import cn.ykf.service.GoodsService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

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

    @Override
    public TradeGoods get(Long goodsId) {
        if (goodsId == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return goodsMapper.selectByPrimaryKey(goodsId);
    }
}
