package cn.ykf.service.impl;

import cn.ykf.PayApplication;
import cn.ykf.constant.ShopCode;
import cn.ykf.model.TradePay;
import cn.ykf.service.PayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 支付模块测试类
 */
@SpringBootTest(classes = PayApplication.class)
public class PayServiceImplTest {

    @Resource
    private PayService payService;

    /**
     * 测试创建支付订单
     */
    @Test
    @DisplayName("创建支付订单")
    void testCreatePayment() {
        TradePay tradePay = new TradePay();
        tradePay.setOrderId(540969440821514240L);
        tradePay.setPayAmount(BigDecimal.valueOf(4880L));
        System.out.println(payService.createPayment(tradePay));
    }

    @Test
    @DisplayName("测试支付回调")
    void testCallbackPayment() throws IOException {
        TradePay pay = new TradePay();
        pay.setOrderId(540969440821514240L);
        pay.setPayId(542054245269114880L);
        pay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        payService.callbackPayment(pay);

        // 防止收不到事务监听
        System.in.read();
    }
}