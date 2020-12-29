package cn.ykf.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 订单消息相关，不用添加@Component
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/28
 */
@Getter
@Setter
@ConfigurationProperties("order")
public class OrderMsgProperties {

    /** 订单消息topic */
    private String topic;
    /** 订单消息tag */
    private OrderTag tag;

    /** 需要静态类，否则无法注入 */
    @Getter
    @Setter
    public static class OrderTag {
        /** 订单确认消息tag */
        private String confirm;
        /** 订单取消消息tag */
        private String cancel;
    }

}
