package com.sohan.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

/**
 * The spring boot application class, that starts the app.
 *
 * @author Sohan
 */
@SpringBootApplication
public class StudentCDCRelayApplication {

	/**
	 * Main method that starts the Spring Boot application.
	 *
	 * @param args Arguments passed to the app.
	 */
	public static void main(String[] args) {
		SpringApplication.run(StudentCDCRelayApplication.class, args);
		LocalDateTime currentDateTime=LocalDateTime.now();
		System.out.println(currentDateTime);//2021-09-12T13:08:56.316
	}
}
