package com.codingcat.aipersonalfinance.module.config.oauth;

import com.codingcat.aipersonalfinance.module.CookieUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

@Schema(description = "OAuth2 인증 중간에 발생하는 정보를 쿠키에 저장했다가 꺼내는 역할")
public class Oauth2AuthorizationRequestBaseOnCookieRepository implements
  AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
  public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQUEST";
  private final static int COOKIE_EXPIRE_SECONDS = 18000;


  // 저장된 쿠키에서 OAuth2 인증 정보를 꺼냄
  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
  }

  // 사용자가 Google 로그인을 시작할 때 필요한 정보를 쿠키에 저장
  @Override
  public void saveAuthorizationRequest(
    OAuth2AuthorizationRequest authorizationRequest,
    HttpServletRequest request, HttpServletResponse response
  ) {
    if(authorizationRequest == null){
      removeAuthorizationRequest(request, response);
      return;
    }
    String cookieContent = CookieUtil.serialize(authorizationRequest);
    CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
      cookieContent, COOKIE_EXPIRE_SECONDS);
  }

  // 인증이 완료되면 더 이상 필요없는 쿠키를 삭제, 해당 정보들은 security context에 저장되어 있으니
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
    HttpServletRequest request, HttpServletResponse response
  ) {
    return this.loadAuthorizationRequest(request);
  }
}
/*
1. 사용자가 구글로 로그인
2. authorizationEndpoint에서 Oauth2AuthorizationRequestBaseOnCookieRepository 호출
  -> saveAuthorizationRequest 실행
  -> OAuth2 요청 정보를 쿠키에 저장
3. 구글 로그인 페이지로 리다이렉트
4. 사용자가 Google에서 승인
5. Google이 callback URL로 authorization code 전송
6. loadAuthorizationRequest 실행
   → 저장된 쿠키에서 요청 정보 로드
   → "맞다, 우리가 한 요청이다" 검증
7. oAuth2UserCustomService가 Google에서 사용자 정보 조회
8. Oauth2SuccessHandler 실행 (여기서 당신의 로직)
*/