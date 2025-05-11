package com.example.shiftmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class ShiftmanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiftmanagementApplication.class, args);
	}

}
