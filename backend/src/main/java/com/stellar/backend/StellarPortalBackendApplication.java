package com.stellar.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StellarPortalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StellarPortalBackendApplication.class, args);
        System.out.println("--------- Ve'ryGood BACKEND STARTED ---------");
    }
}
