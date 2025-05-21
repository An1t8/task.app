package com.example.taskapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

/**
 * Main application class to run the code.
 * @SpringBootApplication enables auto-configuration, component scanning,
 * and configuration properties for the entire application.
 */
@SpringBootApplication
public class TaskappApplication {

	/**
	 * Main method that starts the Spring Boot application.
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(TaskappApplication.class, args);
	}

	//http://localhost:8081/login.html - odkaz na spusteni
}