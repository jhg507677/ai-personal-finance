package com.codingcat.aipersonalfinance.module.config.oauth;

import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.module.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private final AuthService authService;

  // 로그인 성공 시 호출
  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request, HttpServletResponse response, Authentication authentication
  ) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    User user = authService.findByEmail(String.valueOf(oAuth2User.getAttributes().get("email")));
    authService.generateLoginToken(response, user.toAuth());
  }
}
