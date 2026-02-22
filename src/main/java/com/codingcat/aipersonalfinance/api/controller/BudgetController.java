package com.codingcat.aipersonalfinance.api.controller;

import com.codingcat.aipersonalfinance.api.dto.budget.BudgetCreateRequest;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetResponse;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetUpdateRequest;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetUsageResponse;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예산(Budget) 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/client/budgets")
@Tag(name = "Budget", description = "예산 API")
public class BudgetController {

  private final BudgetService budgetService;

  /**
   * 예산 생성
   */
  @PostMapping
  @Operation(summary = "예산 생성", description = "새로운 예산을 생성합니다")
  public ResponseEntity<ApiResponseVo<?>> createBudget(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody BudgetCreateRequest request) {
    log.info("Creating budget for user: {}", userDetails.getUsername());

    BudgetResponse response = budgetService.createBudget(userDetails.getUsername(), request);
    return ResponseEntity.ok(ApiResponseVo.ok(response));
  }

  /**
   * 예산 상세 조회
   */
  @GetMapping("/{budgetId}")
  @Operation(summary = "예산 조회", description = "특정 예산의 상세 정보를 조회합니다")
  public ResponseEntity<ApiResponseVo<?>> getBudget(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    log.info("Getting budget: userId={}, budgetId={}", userDetails.getUsername(), budgetId);

    BudgetResponse response = budgetService.getBudget(userDetails.getUsername(), budgetId);
    return ResponseEntity.ok(ApiResponseVo.ok(response));
  }

  /**
   * 예산 사용 현황 조회
   */
  @GetMapping("/{budgetId}/usage")
  @Operation(summary = "예산 사용 현황 조회", description = "예산 대비 실제 지출 현황을 조회합니다")
  public ResponseEntity<ApiResponseVo<?>> getBudgetUsage(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    log.info("Getting budget usage: userId={}, budgetId={}", userDetails.getUsername(), budgetId);

    BudgetUsageResponse response = budgetService.getBudgetUsage(userDetails.getUsername(), budgetId);
    return ResponseEntity.ok(ApiResponseVo.ok(response));
  }

  /**
   * 예산 수정
   */
  @PutMapping("/{budgetId}")
  @Operation(summary = "예산 수정", description = "예산을 수정합니다")
  public ResponseEntity<ApiResponseVo<?>> updateBudget(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId,
      @Valid @RequestBody BudgetUpdateRequest request) {
    log.info("Updating budget: userId={}, budgetId={}", userDetails.getUsername(), budgetId);

    BudgetResponse response =
        budgetService.updateBudget(userDetails.getUsername(), budgetId, request);
    return ResponseEntity.ok(ApiResponseVo.ok(response));
  }

  /**
   * 예산 삭제 (Soft Delete)
   */
  @DeleteMapping("/{budgetId}")
  @Operation(summary = "예산 삭제", description = "예산을 삭제합니다 (Soft Delete)")
  public ResponseEntity<ApiResponseVo<?>> deleteBudget(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "예산 ID", required = true) @PathVariable Long budgetId) {
    log.info("Deleting budget: userId={}, budgetId={}", userDetails.getUsername(), budgetId);

    budgetService.deleteBudget(userDetails.getUsername(), budgetId);
    return ResponseEntity.ok(ApiResponseVo.ok());
  }

  /**
   * 예산 목록 조회
   */
  @GetMapping
  @Operation(summary = "예산 목록 조회", description = "사용자의 활성화된 예산 목록을 조회합니다")
  public ResponseEntity<ApiResponseVo<?>> getBudgetList(
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Getting budget list for user: {}", userDetails.getUsername());

    List<BudgetResponse> response = budgetService.getBudgetList(userDetails.getUsername());
    return ResponseEntity.ok(ApiResponseVo.ok(response));
  }
}
