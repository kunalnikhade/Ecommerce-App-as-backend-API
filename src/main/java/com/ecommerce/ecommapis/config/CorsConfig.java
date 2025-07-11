package com.ecommerce.ecommapis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig
{
    @Bean
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer()
        {
            @Override
            public void addCorsMappings(final CorsRegistry corsRegistry)
            {
                corsRegistry.addMapping("/**") // Apply to all endpoints
                        .allowedOrigins("http://localhost:192.168.137.133:5000") // Frontend origin
                        .allowedMethods("*") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow cookies (if needed)
            }
        };
    }
}
