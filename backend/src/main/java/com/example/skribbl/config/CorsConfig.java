package com.example.skribbl.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
@Configuration public class CorsConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}") String origins;
    public void addCorsMappings(CorsRegistry r){r.addMapping("/**").allowedOrigins(origins.split(",")).allowedMethods("*").allowedHeaders("*");}
}
