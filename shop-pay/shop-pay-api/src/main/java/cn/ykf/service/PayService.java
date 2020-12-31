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

    /**
     * 支付成功回调
     *
     * @param tradePay 含有订单id
     * @return Result
     */
    Result callbackPayment(TradePay tradePay);

    /**
     * 更新支付订单状态为支付成功
     *
     * @param payId 支付订单id
     * @return Result
     */
    Result updatePaymentToSuccess(Long payId);

    /**
     * 检查订单是否已支付
     *
     * @param payId 订单id
     * @return 已支付 - {@code true}， 未支付 - {@code false}
     */
    boolean checkPayIsPaid(Long payId);
}
