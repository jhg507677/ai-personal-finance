package com.codingcat.aipersonalfinance.module.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 허용할 출처 설정
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

    // 허용할 HTTP 메서드 설정
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // 허용할 헤더 설정
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // 인증 정보 포함 여부
    configuration.setAllowCredentials(true);

    // 최대 캐시 시간 (초)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
