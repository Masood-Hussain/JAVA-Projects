package com.fooddelivery.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the frontend service
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS for API endpoints
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }

    /**
     * Configure static resource handling
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");
            
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");
            
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/");
    }

    /**
     * Configure view controllers for simple pages
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/register").setViewName("register");
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/contact").setViewName("contact");
        registry.addViewController("/privacy").setViewName("privacy");
        registry.addViewController("/terms").setViewName("terms");
    }

    /**
     * WebClient bean for HTTP communication
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
}