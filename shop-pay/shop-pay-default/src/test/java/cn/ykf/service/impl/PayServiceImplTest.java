package cn.ykf.service.impl;

import cn.ykf.PayApplication;
import cn.ykf.model.TradePay;
import cn.ykf.service.PayService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
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
    void testCreatePayment() {
        TradePay tradePay = new TradePay();
        tradePay.setOrderId(666L);
        tradePay.setPayAmount(BigDecimal.TEN);
        System.out.println(payService.createPayment(tradePay));
    }
}