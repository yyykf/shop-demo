package cn.ykf;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品模块主程序
 *
 * @author YuKaiFan <1092882580@qq.com>
 * @date 2020/12/24
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "cn.ykf.service.impl")
@MapperScan("cn.ykf.dao")
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }
}
