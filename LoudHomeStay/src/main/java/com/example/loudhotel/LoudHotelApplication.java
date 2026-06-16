package com.example.loudhotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoudHotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoudHotelApplication.class, args);
    }

}
