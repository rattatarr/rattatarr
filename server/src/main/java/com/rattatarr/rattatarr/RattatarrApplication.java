package com.rattatarr.rattatarr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RattatarrApplication {

    public static void main(String[] args) {
        SpringApplication.run(RattatarrApplication.class, args);
    }

}
