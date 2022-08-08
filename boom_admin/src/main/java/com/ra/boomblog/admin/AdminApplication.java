package com.ra.boomblog.admin;


import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @Author panrenai
 * @Description: 启动类
 * @Date 2022/8/8 16:42
 * @Version 1.0
 */
@EnableTransactionManagement
@SpringBootApplication
@EnableOpenApi
@EnableDiscoveryClient
@EnableCaching
@EnableRabbit
@EnableFeignClients("com.ra.boomblog.commons.feign")
@ComponentScan(basePackages = {
        "com.ra.boomblog.commons.config",
        "com.ra.boomblog.commons.fallback",
        "com.ra.boomblog.utils",
        "com.ra.boomblog.admin",
        "com.ra.boomblog.xo.utils",
        "com.ra.boomblog.xo.service"
})
public class AdminApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(AdminApplication.class, args);
    }

    /**
     * 设置时区
     */
    @PostConstruct
    void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
