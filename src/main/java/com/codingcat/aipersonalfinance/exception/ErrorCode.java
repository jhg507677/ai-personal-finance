package com.codingcat.aipersonalfinance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 공통
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "엔티티를 찾을 수 없습니다"),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다"),

  // Ledger(거래 내역)
  LEDGER_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "거래 내역을 찾을 수 없습니다"),
  LEDGER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "L002", "본인의 거래 내역만 수정/삭제할 수 있습니다"),
  INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "L003", "거래 금액은 0보다 커야 합니다"),
  INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "L004", "시작 날짜는 종료 날짜보다 이전이어야 합니다"),

  // User(사용자)
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
  DUPLICATE_USER_ID(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자 ID입니다");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
