package com.codingcat.aipersonalfinance.module.exception;

import com.codingcat.aipersonalfinance.exception.BusinessException;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

  private HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
    return headers;
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponseVo<?>> handleCustomException(
    CustomException custom
  ) {
    custom.printStackTrace(); // 로깅
    ApiResponseVo<?> response = ApiResponseVo.builder()
      .status(custom.getHttpStatus())
      .code(custom.getCustomCode())
      .message(custom.getMessage())
      .build();
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponseVo<?>> handleBusinessException(BusinessException e) {
    e.printStackTrace(); // 로깅
    ApiResponseVo<?> response = ApiResponseVo.builder()
      .status(e.getErrorCode().getStatus())
      .code(e.getErrorCode().getCode())
      .message(e.getMessage())
      .build();
    return new ResponseEntity<>(response, jsonHeaders(), e.getErrorCode().getStatus());
  }

  /*** @Valid 검증 실패 시 처리*/
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponseVo<?>> handleValidationException(MethodArgumentNotValidException ex) {
    ex.printStackTrace(); // 로깅
    BindingResult bindingResult = ex.getBindingResult();

    // 첫 번째 필드 오류 메시지 가져오기
    FieldError fieldError = bindingResult.getFieldErrors().get(0);

    ApiResponseVo<?> result = ApiResponseVo.builder()
      .status(HttpStatus.BAD_REQUEST)
      .code("sm.common.fail.missing_field")
      .message("해당 필드 '" + fieldError.getField() + "' 오류: " + fieldError.getDefaultMessage())
      .content(fieldError.getRejectedValue())
      .build();

    return new ResponseEntity<>(result, jsonHeaders(), HttpStatus.BAD_REQUEST);
  }

  /*** 헤더 필수 값 누락 시 처리*/
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiResponseVo<?>> handleMissingHeaderException(MissingRequestHeaderException ex) {
    ex.printStackTrace(); // 로깅
    ApiResponseVo<?> result = ApiResponseVo.builder()
      .status(HttpStatus.BAD_REQUEST)
      .code("sm.common.fail.missing_header")
      .message("헤더에 '" + ex.getHeaderName() + "' 값이 누락되어 있습니다.")
      .build();

    return new ResponseEntity<>(result, jsonHeaders(), HttpStatus.BAD_REQUEST);
  }

  /*** 위에서 정의히지 않은 모든 에러*/
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponseVo<?>> handleAllException(Exception ex) {
    ex.printStackTrace(); // 로깅
    ApiResponseVo<?> result = ApiResponseVo.builder()
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .code("sm.common.fail.server_error")
      .message("서버에 예상하지 못한 에러가 발생했습니다. 관리자에게 문의해주세요.")
      .build();

    return new ResponseEntity<>(result, jsonHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
