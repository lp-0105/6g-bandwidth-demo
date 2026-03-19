package com.campus.bandwidth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园去中心化带宽分享激励系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.campus.bandwidth.mapper")
public class BandwidthApplication {
    public static void main(String[] args) {
        SpringApplication.run(BandwidthApplication.class, args);
        System.out.println("======================================");
        System.out.println("  校园带宽分享激励系统 MVP 已启动！");
        System.out.println("  访问地址：http://localhost:8080");
        System.out.println("  H2控制台：http://localhost:8080/h2-console");
        System.out.println("======================================");
    }
}
