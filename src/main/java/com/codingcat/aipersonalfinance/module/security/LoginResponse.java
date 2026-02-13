package com.codingcat.aipersonalfinance.module.security;

import java.time.Instant;
import lombok.Setter;

@Setter
public class LoginResponse {
  private Long userIdx;
  private String accessToken;
  private Instant accessTokenExpire;
}
