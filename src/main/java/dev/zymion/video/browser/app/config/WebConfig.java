package dev.zymion.video.browser.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://192.168.108.13:4200",
                        "http://172.23.240.1:4200",
                        "http://192.168.15.13:4200",
                        "http://localhost:8080",              // <-- DODAJ TO
                        "http://192.168.15.13:8080"           // <-- I TO TEŻ
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

