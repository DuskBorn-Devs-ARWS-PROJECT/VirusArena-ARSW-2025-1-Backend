package edu.eci.arsw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://virusarenafront.s3-website.us-east-2.amazonaws.com",
                                "https://d8c1uwm8l6tk4.cloudfront.net",
                                "https://virusarenaarsw.sytes.net",
                                "backend-app-lb-954081308.us-east-2.elb.amazonaws.com",
                                "http://backend-app-lb-954081308.us-east-2.elb.amazonaws.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}

