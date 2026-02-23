package com.codingcat.aipersonalfinance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

  private final HttpStatus httpStatus;  // 에러 코드나 특정 식별자
  private final String customCode;  // 에러 코드나 특정 식별자
  private final String errorMessage;  // 에러 메시지

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
