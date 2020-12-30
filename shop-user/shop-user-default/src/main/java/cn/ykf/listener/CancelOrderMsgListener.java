package cn.ykf.listener;

import cn.ykf.constant.ShopCode;
import cn.ykf.exception.BusinessException;
import cn.ykf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 取消订单消息监听器 -- 回退余额
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/29
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "${mq.order.consumer}", topic = "${mq.order.topic}")
public class CancelOrderMsgListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Resource
    private UserService userService;

    /**
     * 消费组名
     */
    @Value("${mq.order.consumer}")
    private String consumerGroup;

    /**
     * 无需返回值，如果消费失败抛出异常即可，MQ会尝试重新投递
     * 消费逻辑
     * {@link DefaultRocketMQListenerContainer.DefaultMessageListenerConcurrently#consumeMessage(List, ConsumeConcurrentlyContext)}
     *
     * @param message 待消息消息
     */
    @Override
    public void onMessage(MessageExt message) {
        String tags = message.getTags();
        String msgId = message.getMsgId();
        // 订单id
        String keys = message.getKeys();
        String body = null;
        try {
            body = new String(message.getBody(), RemotingHelper.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("以 {} 方式解码消息内容失败，消息体：{}", RemotingHelper.DEFAULT_CHARSET, message.getBody());
            BusinessException.cast(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL);
        }

        log.info("收到消息：{}", message);

        // 如果出现异常代表消费失败，会重试
        userService.handlerCancelOrderMsg(tags, msgId, keys, body, consumerGroup);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 设置从头消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    }
}
