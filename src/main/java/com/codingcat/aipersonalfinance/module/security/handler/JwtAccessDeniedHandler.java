package com.codingcat.aipersonalfinance.module.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    AccessDeniedException accessDeniedException
  ) throws IOException {
    Authentication authentication = SecurityContextHolder.getContext()
      .getAuthentication();
    String principal = (authentication != null) ? authentication.getPrincipal().toString() : "Anonymous";

    // 인가 실패(필요한 권한이 없이 접근하려 할때 403)
    log.error("Access Denied: {} / Request URI: {} / Principal: {}",
      accessDeniedException.getMessage(),
      request.getRequestURI(),
      principal
    );

    if (authentication != null) {
      authentication.getAuthorities().forEach(auth ->
        log.error("User Role: {}", auth.getAuthority())
      );
    }

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.getWriter().write("auth forbidden");
  }
}
