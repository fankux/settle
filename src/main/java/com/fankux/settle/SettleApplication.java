package com.fankux.settle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fankux")
@MapperScan(basePackages = "com.fankux.dao")
public class SettleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettleApplication.class, args);
    }
}
