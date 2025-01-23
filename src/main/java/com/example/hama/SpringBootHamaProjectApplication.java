package com.example.hama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootHamaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootHamaProjectApplication.class, args);
	}

}
