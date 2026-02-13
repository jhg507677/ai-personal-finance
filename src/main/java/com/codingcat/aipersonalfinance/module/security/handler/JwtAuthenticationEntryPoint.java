package com.codingcat.aipersonalfinance.module.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException authException
  ) throws IOException {
    int status = response.getStatus();
    String errorText;

    if (status == HttpServletResponse.SC_NOT_FOUND) {
      errorText = "Page not found";
    } else {
      errorText = "Unauthorized access - AuthenticationEntryPoint";
    }

    log.error("{} / status: {}", errorText, status);
    response.sendError(status, errorText);
  }
}