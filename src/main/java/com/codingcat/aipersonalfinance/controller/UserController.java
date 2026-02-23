package com.codingcat.aipersonalfinance.controller;


import com.codingcat.aipersonalfinance.domain.user.AddUserRequest;
import com.codingcat.aipersonalfinance.domain.user.UserService;
import com.codingcat.aipersonalfinance.module.response.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserService userService;

  @Operation(summary = "회원가입", description = "")
  @PostMapping("/api/public/v1/client")
  public ResponseEntity<?> signUp(
    @Valid @RequestBody AddUserRequest request
  ){
    return userService.signUp(request);
  }

  @Operation(summary = "로그인", description = "")
  @PostMapping("/api/public/v1/client/login")
  public ResponseEntity<?> signIn(
    HttpServletResponse response,
    @Valid @RequestBody AddUserRequest request
  ){
    return userService.login(response, request);
  }

  @Operation(summary = "리프레시토큰 재발급", description = "")
  @PostMapping("/api/public/v1/client/refresh")
  public ResponseEntity<?> refresh(
    @CookieValue(name = "refreshToken") String refreshToken
  ){
    return userService.refresh(refreshToken);
  }

  @Operation(summary = "유저 상세", description = "")
  @Parameters({@Parameter(name = "id", description = "삭제 공지사항 ID", required = true)})
  @PostMapping("/api/v1/client/{idx}")
  public ResponseEntity<?> signIn(
    @PathVariable(value = "idx") Long idx
  ){
    return userService.getUser(idx);
  }
}


