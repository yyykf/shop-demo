package cn.ykf;

import cn.ykf.config.OrderMsgProperties;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 订单模块主程序
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "cn.ykf.service.impl")
@MapperScan("cn.ykf.dao")
@EnableConfigurationProperties({OrderMsgProperties.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
