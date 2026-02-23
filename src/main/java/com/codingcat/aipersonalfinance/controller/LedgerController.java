package com.codingcat.aipersonalfinance.controller;

import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerCreateRequest;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerSearchCondition;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerUpdateRequest;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerService;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 거래 내역(Ledger) 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Ledger", description = "거래 내역 API")
public class LedgerController {

  private final LedgerService ledgerService;

  @PostMapping("/api/v1/client/ledgers")
  @Operation(summary = "거래 내역 생성", description = "새로운 거래 내역을 생성합니다")
  public ResponseEntity<?> createLedger(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody LedgerCreateRequest request
  ) {
    return ledgerService.createLedger(userPrincipal.getAuthDto(), request);
  }

  /**
   * 거래 내역 상세 조회
   */
  @GetMapping("/api/v1/client/ledgers/{ledgerId}")
  @Operation(summary = "거래 내역 조회", description = "특정 거래 내역의 상세 정보를 조회합니다")
  public ResponseEntity<?> getLedger(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "거래 내역 ID", required = true) @PathVariable Long ledgerId) {
    return ledgerService.getLedger(userPrincipal.getAuthDto(), ledgerId);
  }

  /**
   * 거래 내역 수정
   */
  @PutMapping("/api/v1/client/ledgers/{ledgerId}")
  @Operation(summary = "거래 내역 수정", description = "거래 내역을 수정합니다")
  public ResponseEntity<?> updateLedger(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "거래 내역 ID", required = true) @PathVariable Long ledgerId,
      @Valid @RequestBody LedgerUpdateRequest request) {
    return ledgerService.updateLedger(userPrincipal.getAuthDto(), ledgerId, request);
  }

  /**
   * 거래 내역 삭제 (Soft Delete)
   */
  @DeleteMapping("/api/v1/client/ledgers/{ledgerId}")
  @Operation(summary = "거래 내역 삭제", description = "거래 내역을 삭제합니다 (Soft Delete)")
  public ResponseEntity<?> deleteLedger(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "거래 내역 ID", required = true) @PathVariable Long ledgerId) {
    return ledgerService.deleteLedger(userPrincipal.getAuthDto(), ledgerId);
  }

  /**
   * 거래 내역 목록 조회 (페이징, 필터링, 정렬)
   */
  @GetMapping("/api/v1/client/ledgers")
  @Operation(summary = "거래 내역 목록 조회", description = "거래 내역 목록을 조회합니다 (페이징, 필터링)")
  public ResponseEntity<?> getLedgerList(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "거래 유형 (INCOME/EXPENSE)") @RequestParam(required = false)
          LedgerType type,
      @Parameter(description = "카테고리") @RequestParam(required = false) Category category,
      @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @PageableDefault(size = 20, sort = "recordedDate", direction = Sort.Direction.DESC)
          Pageable pageable) {
    LedgerSearchCondition condition =
        LedgerSearchCondition.builder()
            .type(type)
            .category(category)
            .startDate(startDate)
            .endDate(endDate)
            .build();

    return ledgerService.getLedgerList(userPrincipal.getAuthDto(), condition, pageable);
  }
}
