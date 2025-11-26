package dev.zymion.video.browser.app;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableProcessApplication
@EnableScheduling
public class VideoBrowserAppApplication {

	static void main(String[] args) {
		SpringApplication.run(VideoBrowserAppApplication.class, args);
	}

}
