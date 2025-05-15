package edu.eci.arsw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://virusarena-frontend.s3-website-us-east-1.amazonaws.com") // Solo tu S3
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/ws/**")
                .allowedOrigins("http://virusarena-frontend.s3-website-us-east-1.amazonaws.com") // Solo tu S3
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}