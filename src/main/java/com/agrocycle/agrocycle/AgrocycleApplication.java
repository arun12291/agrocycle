package com.agrocycle.agrocycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgrocycleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgrocycleApplication.class, args);
    }
}
