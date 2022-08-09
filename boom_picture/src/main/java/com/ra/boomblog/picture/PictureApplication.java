package com.ra.boomblog.picture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;


@EnableTransactionManagement
@SpringBootApplication
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients("com.ra.boomblog.commons.feign")
@ComponentScan(basePackages = {
        "com.ra.boomblog.commons.config.feign",
        "com.ra.boomblog.commons.handler",
        "com.ra.boomblog.commons.config.redis",
        "com.ra.boomblog.utils",
        "com.ra.boomblog.picture"})
public class PictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureApplication.class, args);
    }
}
