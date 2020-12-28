package cn.ykf.service.impl;

import cn.ykf.OrderApplication;
import cn.ykf.model.TradeOrder;
import cn.ykf.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 订单业务测试类
 * junit5不需要加@Runwith
 */
@SpringBootTest(classes = OrderApplication.class)
public class OrderServiceImplTest {

    @Resource
    private OrderService orderService;

    /**
     * 测试确认订单
     */
    @Test
    void testConfirmOrder() {
        Long couponId = 1L;
        Long goodsId = 1L;
        Long userId = 345963634385633280L;

        TradeOrder order = new TradeOrder();
        order.setGoodsId(goodsId);
        order.setUserId(userId);
        order.setCouponId(couponId);
        order.setAddress("广东");
        order.setGoodsNumber(1);
        order.setGoodsPrice(new BigDecimal(5000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setOrderAmount(new BigDecimal(5000));
        order.setMoneyPaid(new BigDecimal(100));

        System.out.println(orderService.confirmOrder(order));
    }
}