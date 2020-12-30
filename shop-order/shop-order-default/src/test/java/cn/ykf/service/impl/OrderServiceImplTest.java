package cn.ykf.service.impl;

import cn.ykf.OrderApplication;
import cn.ykf.config.OrderMsgProperties;
import cn.ykf.model.TradeOrder;
import cn.ykf.service.OrderService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 订单业务测试类
 * junit5不需要加@Runwith
 */
@SpringBootTest(classes = OrderApplication.class)
public class OrderServiceImplTest {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderMsgProperties properties;

    @Value("${order.consumer}")
    private String consumer;

    @Resource
    private RocketMQTemplate template;

    /**
     * 测试确认订单
     */
    @Test
    void testConfirmOrder() throws IOException {
        Long couponId = 2L;
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

        // 避免Order服务结束导致监听不到取消消息
        System.in.read();
    }

    /**
     * 测试属性注入
     */
    @Test
    public void testProperties() {
        System.out.println(properties.getTopic());
        System.out.println(properties.getTag().getConfirm());
        System.out.println(properties.getTag().getCancel());
        System.out.println(consumer);
    }

    @Test
    public void testMQ() {
        template.sendOneWay("TEST:tag", MessageBuilder.withPayload("Test").build());
    }
}