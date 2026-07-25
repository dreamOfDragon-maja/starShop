package com.starshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Star Shop 商城系统启动类
 */
@SpringBootApplication
@MapperScan("com.starshop.mapper")
public class StarShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarShopApplication.class, args);
    }
}
public