package cn.ykf.service.impl;

import cn.ykf.constant.ShopCode;
import cn.ykf.dao.TradePayMapper;
import cn.ykf.entity.Result;
import cn.ykf.exception.BusinessException;
import cn.ykf.model.TradePay;
import cn.ykf.model.TradePayExample;
import cn.ykf.service.PayService;
import cn.ykf.util.IdHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 支付业务实现类
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/30
 */
@Slf4j
@DubboService
public class PayServiceImpl implements PayService {

    @Resource
    private TradePayMapper payMapper;

    @Override
    public Result createPayment(TradePay tradePay) {
        if (tradePay == null || tradePay.getOrderId() == null || tradePay.getPayAmount() == null) {
            BusinessException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        // 查询是否已经支付过
        TradePayExample condition = new TradePayExample();
        condition.createCriteria()
                .andOrderIdEqualTo(tradePay.getOrderId())
                .andIsPaidEqualTo(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        int count = payMapper.countByExample(condition);
        if (count > 0) {
            log.warn("订单 {} 已经支付过", tradePay.getOrderId());
            BusinessException.cast(ShopCode.SHOP_PAYMENT_IS_PAID);
        }

        tradePay.setPayId(IdHelper.getNewId(TradePayMapper.class));
        tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());

        return payMapper.insert(tradePay) > 0 ? Result.of(ShopCode.SHOP_SUCCESS) : Result.of(ShopCode.SHOP_FAIL);
    }
}
