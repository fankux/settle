package com.fl.settle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fl")
@MapperScan(basePackages = "com.fl.dao")
public class SettleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettleApplication.class, args);
    }
}
