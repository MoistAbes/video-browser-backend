package dev.zymion.video.browser.app;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
public class VideoBrowserAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoBrowserAppApplication.class, args);
	}

}
