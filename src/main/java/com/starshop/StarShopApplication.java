package com.starshop;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Star Shop 商城系统启动类
 */
@SpringBootApplication
@Slf4j
@MapperScan("com.starshop.mapper")
public class StarShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarShopApplication.class, args);
        log.info("server started");
    }
}
