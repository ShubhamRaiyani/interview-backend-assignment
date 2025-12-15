package com.shubham.internship_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InternshipBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternshipBackendApplication.class, args);
	}

}
