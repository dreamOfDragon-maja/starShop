package com.starshop;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Star Shop 商城系统启动类
 */
@SpringBootApplication
@Slf4j
@EnableScheduling
@MapperScan("com.starshop.mapper")
@EnableAspectJAutoProxy
public class StarShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarShopApplication.class, args);
        log.info("server started");
    }
}
