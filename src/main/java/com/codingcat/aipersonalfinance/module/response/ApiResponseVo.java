package com.codingcat.aipersonalfinance.module.response;

import static com.codingcat.aipersonalfinance.module.model.ImportanceLevel.LOG_ONLY;

import com.codingcat.aipersonalfinance.module.model.ImportanceLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Slf4j
public class ApiResponseVo<T> {
  @Schema(description = "HTTP 상태코드")
  private HttpStatus status;

  @Schema(description = "메시지 코드", example = "sm.common.success.default")
  private String code;

  @Schema(description = "한글 메시지")
  private String message;

  @Schema(description = "한글 메시지(국제화 도입시 사용)")
  private String enMessage;

  @Schema(description = "실제 반환 데이터")
  private T content;

  @JsonIgnore
  @Schema(hidden = true, description = "가공용 에러 객체")
  private Exception error;

  @JsonIgnore
  @Default
  @Schema(description = "응답 내용을 DB에다가 저장할때 중요도")
  private ImportanceLevel importance = LOG_ONLY;

  public static ApiResponseVo<Object> ok(Object content) {
    return ApiResponseVo.builder().status(HttpStatus.OK).code("sm.common.success.default").message("success").content(content).build();
  }

  public static ApiResponseVo<Object> ok() {
    return ApiResponseVo.builder().status(HttpStatus.OK).code("sm.common.success.default").message("success").build();
  }

  public static ApiResponseVo<?> failServer(Exception e) {
    e.printStackTrace();
    log.error("[{}]", "sm.common.fail.server", "서버에 문제가 발생하여 해당 요청에 실패하였습니다. 관리자에게 문의바랍니다.");
    return ApiResponseVo.builder()
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .code("sm.common.fail.server")
      .message("서버에 문제가 발생하여 해당 요청에 실패하였습니다. 관리자에게 문의바랍니다.")
      .content(null)
      .build();
  }
}