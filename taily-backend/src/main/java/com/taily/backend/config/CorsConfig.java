package com.taily.backend.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${taily.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
  private String allowedOriginPatterns;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    List<String> patterns =
        allowedOriginPatterns == null || allowedOriginPatterns.isBlank()
            ? List.of("http://localhost:*", "http://127.0.0.1:*")
            : List.of(allowedOriginPatterns.split("\\s*,\\s*"));
    registry
        .addMapping("/**")
        .allowedOriginPatterns(patterns.toArray(String[]::new))
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("*")
        .maxAge(3600);
  }
}

