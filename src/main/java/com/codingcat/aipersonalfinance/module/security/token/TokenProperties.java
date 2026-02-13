package com.codingcat.aipersonalfinance.module.security.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")  // `jwt`로 시작하는 설정들을 모두 자동으로 매핑
public class TokenProperties {
  private String ISSUER;
  private String ADMIN_SECRET;
  private String USER_SECRET;
  private long ACCESS_EXPIRE_TIME;
  private long REFRESH_EXPIRE_TIME;
}
