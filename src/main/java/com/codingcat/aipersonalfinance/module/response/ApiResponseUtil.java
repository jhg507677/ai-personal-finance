package com.codingcat.aipersonalfinance.module.response;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiResponseUtil<T> {
  private static HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
    return headers;
  }

  private static  void printError(ApiResponseVo<?> apiResponseVo){
    if (apiResponseVo.getError() != null) {
      apiResponseVo.getError().printStackTrace();
      log.error("[{}]", apiResponseVo.getCode(), apiResponseVo.getError());
    } else {
      log.error("[{}] {}", apiResponseVo.getCode(), apiResponseVo.getMessage());
    }
    apiResponseVo.setError(null);
  }

  public static <T> ResponseEntity<ApiResponseVo<?>> sendApiResponse(ApiResponseVo<T> apiResponseVo) {
    printError(apiResponseVo);
    return ResponseEntity.status(apiResponseVo.getStatus())
      .headers(jsonHeaders())
      .body(apiResponseVo);
  }

  public static <T> ResponseEntity<ApiResponseVo<?>> sendApiResponse(
    HttpStatus status,
    String code,
    String message,
    T data,
    Exception e
  ) {
    // ApiResponseVo 생성
    ApiResponseVo<T> response = ApiResponseVo.<T>builder()
      .status(status)
      .code(code)
      .message(message)
      .content(data)
      .error(e)
      .build();
    printError(response);
    return new ResponseEntity<>(response, jsonHeaders(), status);
  }

  public static <T> ResponseEntity<ApiResponseVo<?>> sendApiResponseFailServer(Exception e) {
    ApiResponseVo<T> response = ApiResponseVo.<T>builder()
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .code("sm.common.fail.server")
      .message("서버에 문제가 발생하여 해당 요청에 실패하였습니다. 관리자에게 문의바랍니다.")
      .content(null)
      .error(e)
      .build();
    printError(response);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .headers(jsonHeaders())
      .body(response);
  }

  /**
   * 성공 응답 헬퍼 메서드
   */
  public static <T> ResponseEntity<?> sendApiOK(T data) {
    return sendApiResponse(
        HttpStatus.OK,
        "sm.common.success.default",
        "success",
        data,
        null);
  }

  /**
   * 성공 응답 헬퍼 메서드
   */
  public static <T> ResponseEntity<?> sendApiOKByDT(T data) {
    return sendApiResponse(
      HttpStatus.OK,
      "sm.common.success.default",
      "success",
      data,
      null);
  }
}