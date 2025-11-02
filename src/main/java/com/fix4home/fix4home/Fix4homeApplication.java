package com.fix4home.fix4home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
	info = @Info(
		title = "Fix4Home API",
		version = "1.0",
		description = "API Documentation for Fix4Home platform"
	)
)
public class Fix4homeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Fix4homeApplication.class, args);
	}

}
