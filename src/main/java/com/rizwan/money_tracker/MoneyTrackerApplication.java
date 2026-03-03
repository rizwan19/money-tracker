package com.rizwan.money_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTrackerApplication.class, args);
	}

}
