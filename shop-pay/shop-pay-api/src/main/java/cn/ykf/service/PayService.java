package cn.ykf.service;

import cn.ykf.entity.Result;
import cn.ykf.model.TradePay;

/**
 * 支付业务接口
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/30
 */
public interface PayService {

    /**
     * 创建支付订单
     *
     * @param tradePay 含有订单id和金额的支付订单
     * @return Result
     */
    Result createPayment(TradePay tradePay);
}
