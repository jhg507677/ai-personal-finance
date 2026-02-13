package com.codingcat.aipersonalfinance.module.security;

import static com.codingcat.aipersonalfinance.module.model.ApiResponseUtil.sendApiResponseFailServer;

import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.CookieUtil;
import com.codingcat.aipersonalfinance.module.exception.CustomException;
import com.codingcat.aipersonalfinance.module.model.ApiResponseUtil;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.module.security.token.RefreshToken;
import com.codingcat.aipersonalfinance.module.security.token.RefreshTokenRepository;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider.JWT_STATUS;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider.TokenResult;
import com.codingcat.aipersonalfinance.module.security.token.TokenType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository tokenRepository;
  private final UserRepository userRepository;


  /*** 유저 아이디로 유저 정보 가져오기*/
  public User findByUserId(String userId) {
    return userRepository.findByUserId(userId)
      .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "sm.common.fail.invalid_invalid_request","로그인 할 수없는 계정입니다."));
  }

  /*** 유저 이메일로 유저 정보 가져오기*/
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
      .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "sm.common.fail.invalid_invalid_request","로그인 할 수없는 계정입니다."));
  }

  // 토큰 생성하기
  public ApiResponseVo<?> generateLoginToken(
    HttpServletResponse response,
    AuthDto auth
  ){
    Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
    try {
      TokenResult accessToken = tokenProvider.makeToken(TokenType.ACCESS, auth, currentTimestamp.getTime());
      TokenResult refreshToken = tokenProvider.makeToken(TokenType.REFRESH, auth, currentTimestamp.getTime());
      RefreshToken refreshTokenBox = RefreshToken.builder()
        .refreshToken(refreshToken.token())
        .expiredDateTime(refreshToken.expiresAt())
        .userIdx(auth.getUserIdx())
        .build();
      // 생성한 토큰들 DB에 저장
      tokenRepository.save(refreshTokenBox);

      // 응답
      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setAccessToken(accessToken.token());
      loginResponse.setAccessTokenExpire(accessToken.expiresAt());
      loginResponse.setUserIdx(auth.getUserIdx());

      CookieUtil.addSecureCookie(response, tokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken.token(), Duration.ofDays(7));
      return ApiResponseVo.ok(loginResponse);
    }catch (Exception e){
      return ApiResponseVo.failServer(e);
    }
  }

  // refresh 토큰으로 새로운 AccessToken을 갱신
  @Transactional
  public String createNewAccessToken(String refreshToken){
    if(tokenProvider.validateToken(refreshToken) != JWT_STATUS.VALID){
      throw new CustomException(HttpStatus.BAD_REQUEST, "sm.common.fail.invalid_token","올바르지 않은 토큰 정보입니다.");
    }

    RefreshToken dbRefreshToken = tokenRepository.findByRefreshToken(refreshToken)
      .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "sm.common.fail.invalid_token_db", "올바르지 않은 토큰 정보입니다."));

    User user = userRepository.findById(dbRefreshToken.getUserIdx())
      .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "sm.common.fail.invalid_user", "존재하지 않는 유저입니다."));

    Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
    TokenResult tokenResult = tokenProvider.makeToken(TokenType.ACCESS, user.toAuth(), currentTimestamp.getTime());
    dbRefreshToken.increaseRefreshCount();

    return tokenResult.token();
  }
}
