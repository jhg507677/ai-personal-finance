package com.codingcat.aipersonalfinance.api.controller;


import com.codingcat.aipersonalfinance.api.service.user.AddUserRequest;
import com.codingcat.aipersonalfinance.api.service.user.UserService;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import javax.swing.Spring;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {
  private final UserService userService;

  @Operation(summary = "회원가입", description = "")
  @PostMapping("/api/public/v1/client")
  public ResponseEntity<ApiResponseVo<?>> signUp(
    @Valid @RequestBody AddUserRequest request
  ){
    return userService.signUp(request);
  }

  @Operation(summary = "로그인", description = "")
  @PostMapping("/api/public/v1/client/login")
  public ResponseEntity<ApiResponseVo<?>> signIn(
    HttpServletResponse response,
    @Valid @RequestBody AddUserRequest request
  ){
    return userService.login(response, request);
  }

  @Operation(summary = "리프레시토큰 재발급", description = "")
  @PostMapping("/api/public/v1/client/refresh")
  public ResponseEntity<ApiResponseVo<?>> refresh(
    @CookieValue(name = "refreshToken") String refreshToken
  ){
    return userService.refresh(refreshToken);
  }

  @Operation(summary = "유저 상세", description = "")
  @Parameters({@Parameter(name = "id", description = "삭제 공지사항 ID", required = true)})
  @PostMapping("/api/v1/client/{idx}")
  public ResponseEntity<ApiResponseVo<?>> signIn(
    @PathVariable(value = "idx") Long idx
  ){
    return userService.getUser(idx);
  }
}


