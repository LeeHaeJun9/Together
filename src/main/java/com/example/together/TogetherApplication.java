package com.example.together;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TogetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(TogetherApplication.class, args);
    }

}
