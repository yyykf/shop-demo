package cn.ykf.listener;

import cn.ykf.model.vo.PayVo;
import cn.ykf.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 支付成功消息监听器，泛型就是消息中的内容，无需 {@code Message<Payvo>}
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/31
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${pay.topic}", consumerGroup = "${pay.consumer}")
public class PayMsgListener implements RocketMQListener<PayVo>, RocketMQPushConsumerLifecycleListener {

    @Resource
    private OrderService orderService;

    @Override
    public void onMessage(PayVo vo) {
        log.info("收到支付成功消息");
        log.info("订单id: {}", vo.getOrderId());

        orderService.updateOrderToIsPaid(vo.getOrderId());
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    }
}
