package com.codingcat.aipersonalfinance.controller;

import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetCreateRequest;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetUpdateRequest;
import com.codingcat.aipersonalfinance.domain.budget.BudgetService;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Schema(description = "예산(Budget) 컨트롤러")
@RequiredArgsConstructor
@RestController
@Tag(name = "Budget", description = "예산 API")
public class BudgetController {
  private final BudgetService budgetService;

  @PostMapping("/api/v1/client/budgets")
  @Operation(summary = "예산 생성", description = "새로운 예산을 생성합니다")
  public ResponseEntity<?> createBudget(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody BudgetCreateRequest request) {
    return budgetService.createBudget(userPrincipal.getAuthDto(), request);
  }

  /**
   * 예산 상세 조회
   */
  @GetMapping("/api/v1/client/budgets/{budgetId}")
  @Operation(summary = "예산 조회", description = "특정 예산의 상세 정보를 조회합니다")
  public ResponseEntity<?> getBudget(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    return budgetService.getBudget(userPrincipal.getAuthDto(), budgetId);
  }


  /**
   * 예산 사용 현황 조회
   */
  @GetMapping("/api/v1/client/budgets/{budgetId}/usage")
  @Operation(summary = "예산 사용 현황 조회", description = "예산 대비 실제 지출 현황을 조회합니다")
  public ResponseEntity<?> getBudgetUsage(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    return budgetService.getBudgetUsage(userPrincipal.getAuthDto(), budgetId);
  }

  /**
   * 예산 수정
   */
  @PutMapping("/api/v1/client/budgets/{budgetId}")
  @Operation(summary = "예산 수정", description = "예산을 수정합니다")
  public ResponseEntity<?> updateBudget(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId,
      @Valid @RequestBody BudgetUpdateRequest request) {
    return budgetService.updateBudget(userPrincipal.getAuthDto(), budgetId, request);
  }

  /**
   * 예산 삭제 (Soft Delete)
   */
  @DeleteMapping("/api/v1/client/budgets/{budgetId}")
  @Operation(summary = "예산 삭제", description = "예산을 삭제합니다 (Soft Delete)")
  public ResponseEntity<?> deleteBudget(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    return budgetService.deleteBudget(userPrincipal.getAuthDto(), budgetId);
  }

  /**
   * 예산 목록 조회
   */
  @GetMapping("/api/v1/client/budgets")
  @Operation(summary = "예산 목록 조회", description = "사용자의 활성화된 예산 목록을 조회합니다")
  public ResponseEntity<?> getBudgetList(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return budgetService.getBudgetList(userPrincipal.getAuthDto());
  }
}
