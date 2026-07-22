package com.cognizant.logitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class ComplianceDocApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComplianceDocApplication.class, args);
    }
}
