package com.codingcat.aipersonalfinance.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * 테스트용 JPA 설정
 * JPA Auditing 기능을 활성화합니다.
 */
@TestConfiguration
@EnableJpaAuditing
public class TestJpaConfig {
  // auditorProvider는 메인 애플리케이션에 정의되어 있지 않으므로 여기서 제공
  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional.of("test-user");
  }
}
