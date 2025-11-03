package com.fix4home.fix4home;

import com.fix4home.fix4home.config.FileStorageConfig;
import com.fix4home.fix4home.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableConfigurationProperties(FileStorageConfig.class)
public class Fix4homeApplication implements CommandLineRunner {

	@Autowired
	private FileStorageService fileStorageService;

	public static void main(String[] args) {
		SpringApplication.run(Fix4homeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize file storage on application startup
		fileStorageService.init();
		System.out.println("File storage initialized successfully");
	}
}
