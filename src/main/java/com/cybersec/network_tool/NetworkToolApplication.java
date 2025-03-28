package com.cybersec.network_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cybersec")
public class NetworkToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkToolApplication.class, args);
	}

}
