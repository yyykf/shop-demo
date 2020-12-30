package cn.ykf;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Web端主程序
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@EnableDubbo
@SpringBootApplication
public class ShopWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopWebApplication.class, args);
    }
}
