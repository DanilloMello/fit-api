package com.connecthealth.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.connecthealth")
@EntityScan(basePackages = "com.connecthealth")
@EnableJpaRepositories(basePackages = "com.connecthealth")
@ConfigurationPropertiesScan("com.connecthealth")
public class ConnectHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectHealthApplication.class, args);
    }
}
