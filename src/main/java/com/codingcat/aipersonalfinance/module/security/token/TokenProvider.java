package com.codingcat.aipersonalfinance.module.security.token;

import com.codingcat.aipersonalfinance.module.model.ServiceType;
import com.codingcat.aipersonalfinance.module.security.AuthDto;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenProvider implements InitializingBean{
  @Autowired TokenProperties tokenProperties;

  private static final int REFRESH_TOKEN_USE_LIMIT = 5;

  public final String TOKEN_PREFIX = "Bearer ";
  public final String HEADER_AUTHORIZATION = "Authorization";
  public final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private Key ADMIN_KEY;
  private Key USER_KEY;

  @Override
  public void afterPropertiesSet(){
    // 비밀값과 함께 HS256 방식으로 암호화
    this.ADMIN_KEY = Keys.hmacShaKeyFor(tokenProperties.getADMIN_SECRET().getBytes(StandardCharsets.UTF_8));
    this.USER_KEY = Keys.hmacShaKeyFor(tokenProperties.getUSER_SECRET().getBytes(StandardCharsets.UTF_8));
  }
  // ****************************************************************************

  public enum JWT_STATUS {
    VALID,
    UNSUPPORTED,
    MALFORMED,
    INVALID_SIGNATURE,
    EXPIRED,
    ERROR
  }

  // 발행된 JWT를 보고 서비스 타입을 가져와 그게 맞는 SecretKey 가져오기
  private Key resolveSecretKeyByJwt(String jwt){
    return switch (getServiceTypeByToken(jwt)) {
      case USER -> this.USER_KEY;
      case ADMIN -> this.ADMIN_KEY;
      default -> null;
    };
  }

  private Key getSecretKeyByServiceType(ServiceType serviceType){
    return switch (serviceType) {
      case USER -> this.USER_KEY;
      case ADMIN -> this.ADMIN_KEY;
      default -> null;
    };
  }

  public record TokenResult(
    String token,
    Instant expiresAt
  ) {}

  /**
   * 토큰 생성
   * @param tokenType
   * @param auth
   * @param curTimestamp : 현재 시간
   * @return
   */
  public TokenResult makeToken(
    TokenType tokenType,
    AuthDto auth,
    long curTimestamp
  ) {
    Key secretKey = getSecretKeyByServiceType(auth.getServiceType());

    long expireTimeMs = switch (tokenType) {
      case ACCESS -> tokenProperties.getACCESS_EXPIRE_TIME();
      case REFRESH -> tokenProperties.getREFRESH_EXPIRE_TIME();
    };

    Instant expiresAt = Instant.ofEpochMilli(curTimestamp + expireTimeMs);

    String token = Jwts.builder()
      .setHeaderParam("SERVICE_TYPE", auth.getServiceType().name())
      .setIssuer(tokenProperties.getISSUER())
      .setIssuedAt(new Date(curTimestamp))
      .setSubject(auth.getAuthId())
      .claim("IDX", String.valueOf(auth.getAuthIdx()))
      .setExpiration(Date.from(expiresAt))
      .signWith(secretKey)
      .compact();
    return new TokenResult(token, expiresAt);
  }

  public JWT_STATUS validateToken(String jwt) {
    try {
      getClaims(jwt);
      return JWT_STATUS.VALID;
    } catch (ExpiredJwtException e) {
      logTokenError(e);
      return JWT_STATUS.EXPIRED;
    } catch (SecurityException e) {
      logTokenError(e);
      return JWT_STATUS.INVALID_SIGNATURE;
    } catch (JwtException | IllegalArgumentException e) {
      logTokenError(e);
      return JWT_STATUS.MALFORMED;
    }
  }

  private void logTokenError(Exception e) {
    log.error("올바른 JWT 토큰이 아닙니다", e);
  }

  private Jws<Claims> getClaims(String jwt) {
    Key secretKey = resolveSecretKeyByJwt(jwt);
    try {
      return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwt);
    } catch (Exception e) {
      throw e;
    }
  }

  // Prefix 부분 삭제하기
  public String deleteTokenPrefix(String _token){
    if(_token.startsWith(TOKEN_PREFIX))
      return _token.substring(TOKEN_PREFIX.length());
    else
      return _token;
  }

  // 토큰의 헤더에서 subject값 가져오기
  public String getAuthIdFromToken(String jwt) {
    Claims claims = getClaims(jwt).getBody();
    return claims.getSubject();
  }

  // 토큰의 바디에서 authIdx값 가져오기
  public Long getAuthIdxFromToken(String jwt) {
    Claims claims = getClaims(jwt).getBody();
    return claims.get("IDX", Long.class);
  }

  // 토큰의 헤더에서 서비스 타입 가져오기
  public ServiceType getServiceTypeByToken(String jwt) {
    // JWT에서 헤더만 추출 (첫 번째 '.' 까지 잘라냄)
    int i = jwt.lastIndexOf('.');
    String jwtWithoutSignature = jwt.substring(0, i+1);

    return ServiceType.valueOf(Jwts.parserBuilder().build().parseClaimsJwt(jwtWithoutSignature).getHeader().get("SERVICE_TYPE").toString());
  }

  // 토큰에서 인증 객체 가져오기
  public UsernamePasswordAuthenticationToken  getAuthentication(ServiceType serviceType, String token){
    String role = "";
    Claims claims = getClaims(token).getBody();

    if (serviceType.equals(ServiceType.USER)){
      role = "ROLE_USER";
    }else{
      role = "ROLE_ADMIN";
    }
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

    // UserPrincipal 생성 (user의 인증 정보를 포함)
    UserPrincipal userPrincipal = new UserPrincipal(claims.getSubject(), "", role);

    return new UsernamePasswordAuthenticationToken(userPrincipal, token, authorities);
  }
}
