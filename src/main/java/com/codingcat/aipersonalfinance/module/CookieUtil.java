package com.codingcat.aipersonalfinance.module;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.time.Duration;
import java.util.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieUtil {
  // 요청값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
  public static void addCookie(
    HttpServletResponse response, String name, String value, int maxAge
  ){
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  /*** httpOnly로 리프레시 토큰 저장*/
  public static void addSecureCookie(
    HttpServletResponse response, String name, String value, Duration maxAge
  ){
    ResponseCookie refreshCookie = ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(true)
      .path("/")
      .maxAge(maxAge)
      .sameSite("Strict")
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  // 쿠키의 이름을 입력받아 쿠키 삭제
  // 실제 쿠키를 삭제하는 방법이 없으므로 빈값으로 덮어씌움
  public static void deleteCookie(
    HttpServletRequest request, HttpServletResponse response, String name
  ){
    Cookie[] cookies = request.getCookies();
    if(cookies == null) return;

    for(Cookie cookie : cookies){
      if(name.equals(cookie.getName())){
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }

  /**
   * 객체를 직렬화해 쿠키의 값으로 변환
   * @param object : OAuth2AuthorizationRequest authorizationRequest
   * @return
   */
  public static String serialize(Object object){
    return Base64.getUrlEncoder().encodeToString(
      SerializationUtils.serialize((Serializable) object)
    );
  }

  // 쿠키를 역직렬화 -> 객체로 변환
  public static <T> T deserialize(Cookie cookie, Class<T> tClass){
    return tClass.cast(
      SerializationUtils.deserialize(
        Base64.getUrlDecoder().decode(cookie.getValue())
      )
    );
  }
}
