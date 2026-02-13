package com.codingcat.aipersonalfinance.module.security.filter;

import com.codingcat.aipersonalfinance.module.model.ServiceType;
import com.codingcat.aipersonalfinance.module.security.AdminDetailService;
import com.codingcat.aipersonalfinance.module.security.AdminPrincipal;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider.JWT_STATUS;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
  private final TokenProvider tokenProvider;
  private final AdminDetailService adminDetailService;
  private final UserDetailsService userDetailsService;
  // =================================================================================

  /**
   * HttpServletRequest Header에서 토큰 정보를 꺼내오는 메소드(토큰이 1개임을 전제)
   * @param request
   * @return
   */
  private Optional<String> getBearerTokenByHeader(HttpServletRequest request) {
    String bearerToken = request.getHeader(tokenProvider.HEADER_AUTHORIZATION);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenProvider.TOKEN_PREFIX)) {
      return Optional.ofNullable(tokenProvider.deleteTokenPrefix(bearerToken));
    }
    return Optional.empty();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
      ) throws ServletException, IOException {
    ServiceType SERVICE_TYPE;
    Long USER_IDX;

    // ***** 1. 토큰 가져오기
    Optional<String> token = getBearerTokenByHeader(request);
    String servletPath = request.getServletPath();
      if(token.isEmpty()){
      if(servletPath.contains("/api/public")){
        filterChain.doFilter(request, response);
        return;
      } else{
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("sm.auth.fail.token_not_found");
        return;
      }
    }

    // ***** 2. 토큰을 검증한다
    JWT_STATUS jwtStatus = tokenProvider.validateToken(token.get());
    if(jwtStatus.equals(JWT_STATUS.EXPIRED)) {
      log.error("API_AUTH_FAIL : 만료된 토큰입니다. servletPath : "+servletPath);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("sm.api.fail.expired_token");
      return;
    } else if(!jwtStatus.equals(JWT_STATUS.VALID)) {
      log.error("API_AUTH_FAIL : 올바르지 않은 토큰입니다: " + jwtStatus);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("sm.auth.fail.invalid_token");
      return;
    }
    
    // ***** 4. wserviceCode와 wuserIdx 정보를 토큰에서 꺼낸다
    try {
      SERVICE_TYPE = tokenProvider.getServiceTypeByToken(token.get());
      USER_IDX = tokenProvider.getAuthIdxFromToken(token.get());

      if (SERVICE_TYPE == null || USER_IDX == null) {
        log.error("API_AUTH_FAIL : 토큰에 필수 정보가 누락됐습니다.");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("sm.auth.fail.missing_required_information_token");
        return;
      }
    } catch(Exception e) {
      log.error("API_AUTH_FAIL : 토큰을 읽는 과정에서 오류가 발생했습니다: ", e);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("sm.auth.fail.read_token");
      return;
    }

    try{
      // 토큰에 저장된 정보를 토대로 해당 웹서비스에 맞는 User->Principal->Authentication 객체를 불러와서 Filter에 넘긴다.
      Authentication authObject = null;
      if(SERVICE_TYPE.equals(ServiceType.ADMIN)) {
        AdminPrincipal adminPrincipal = (AdminPrincipal) adminDetailService.loadUserByUsername(USER_IDX.toString());
        authObject = new UsernamePasswordAuthenticationToken(
          adminPrincipal,
          adminPrincipal.getPassword(),
          adminPrincipal.getAuthorities()
        );
      }else if(SERVICE_TYPE.equals(ServiceType.USER)) {
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(USER_IDX.toString());
        authObject = new UsernamePasswordAuthenticationToken(
          userPrincipal,
          userPrincipal.getPassword(),
          userPrincipal.getAuthorities()
        );
      } else{
        log.error("API_AUTH_FAIL : 정의되지 않은 서비스 타입입니다.");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write("sm.auth.fail.undefined_service_type");
        return;
      }
      SecurityContextHolder.getContext().setAuthentication(authObject);
    }catch (Exception e){
      log.error("처리 중 예외 발생", e);
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.getWriter().write("sm.api.fail.exception");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedForHeader = request.getHeader("X-Forwarded-For");
    if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
      return xForwardedForHeader.split(",")[0].trim(); // 첫 번째 IP 주소를 반환
    }
    return request.getRemoteAddr(); // 프록시가 없으면 원래 IP 반환
  }
}