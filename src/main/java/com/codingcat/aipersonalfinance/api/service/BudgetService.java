package com.codingcat.aipersonalfinance.api.service;

import com.codingcat.aipersonalfinance.api.dto.budget.BudgetCreateRequest;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetResponse;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetUpdateRequest;
import com.codingcat.aipersonalfinance.api.dto.budget.BudgetUsageResponse;
import com.codingcat.aipersonalfinance.domain.budget.Budget;
import com.codingcat.aipersonalfinance.domain.budget.BudgetRepository;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerRepository;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.exception.CustomException;
import com.codingcat.aipersonalfinance.module.model.ApiResponseUtil;
import com.codingcat.aipersonalfinance.module.security.AuthDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Budget(예산) 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

  private final BudgetRepository budgetRepository;
  private final UserRepository userRepository;
  private final LedgerRepository ledgerRepository;

  @Transactional
  public ResponseEntity<?> createBudget(AuthDto authDto, BudgetCreateRequest request) {
    User user = findUserByUserId(authDto.getUserId());

    // 같은 카테고리, 같은 기간에 예산이 이미 존재하는지 확인
    validateDuplicatePeriod(user, request);

    Budget budget =
        Budget.builder()
            .user(user)
            .name(request.getName())
            .budgetPeriod(request.getBudgetPeriod())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .amount(request.getAmount())
            .category(request.getCategory())
            .alertThreshold(
                request.getAlertThreshold() != null
                    ? request.getAlertThreshold()
                    : new BigDecimal("80.00"))
            .isActive(true)
            .isAlertSent(false)
            .build();

    Budget savedBudget = budgetRepository.save(budget);
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK,
        "sm.common.success.default",
        "success",
        BudgetResponse.from(savedBudget),
        null);
  }

  public ResponseEntity<?> getBudget(AuthDto authDto, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(authDto.getUserId(), budget);
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK,
        "sm.common.success.default",
        "success",
        BudgetResponse.from(budget),
        null);
  }

  public ResponseEntity<?> getBudgetUsage(AuthDto authDto, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(authDto.getUserId(), budget);

    // 해당 기간 내 실제 지출 집계
    BigDecimal totalSpent =
        ledgerRepository.calculateTotalByTypeAndDateRange(
            budget.getUser(),
            LedgerType.EXPENSE,
            budget.getStartDate(),
            budget.getEndDate());

    // 남은 예산 계산
    BigDecimal remainingAmount = budget.getAmount().subtract(totalSpent);

    // 사용률 계산 (%)
    BigDecimal usagePercentage =
        totalSpent
            .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);

    // 예산 초과 여부
    boolean isExceeded = totalSpent.compareTo(budget.getAmount()) > 0;

    // 알림 발송 필요 여부 (사용률이 임계값 초과 && 아직 알림 미발송)
    boolean shouldAlert =
        usagePercentage.compareTo(budget.getAlertThreshold()) >= 0
            && !budget.getIsAlertSent();

    BudgetUsageResponse response =
        BudgetUsageResponse.builder()
            .budget(BudgetResponse.from(budget))
            .totalSpent(totalSpent)
            .remainingAmount(remainingAmount)
            .usagePercentage(usagePercentage)
            .isExceeded(isExceeded)
            .shouldAlert(shouldAlert)
            .build();

    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK, "sm.common.success.default", "success", response, null);
  }

  @Transactional
  public ResponseEntity<?> updateBudget(
      AuthDto authDto, Long budgetId, BudgetUpdateRequest request) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(authDto.getUserId(), budget);

    budget.update(
        request.getName(),
        request.getBudgetPeriod(),
        request.getStartDate(),
        request.getEndDate(),
        request.getAmount(),
        request.getCategory(),
        request.getAlertThreshold(),
        request.getIsActive());

    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK,
        "sm.common.success.default",
        "success",
        BudgetResponse.from(budget),
        null);
  }

  @Transactional
  public ResponseEntity<?> deleteBudget(AuthDto authDto, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(authDto.getUserId(), budget);
    budget.softDelete();
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK, "sm.common.success.default", "success", null, null);
  }

  public ResponseEntity<?> getBudgetList(AuthDto authDto) {
    User user = findUserByUserId(authDto.getUserId());
    List<Budget> budgets = budgetRepository.findByUserAndIsActiveTrue(user);
    List<BudgetResponse> responses =
        budgets.stream().map(BudgetResponse::from).collect(Collectors.toList());
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK, "sm.common.success.default", "success", responses, null);
  }

  /**
   * 기간 중복 검증
   */
  private void validateDuplicatePeriod(User user, BudgetCreateRequest request) {
    Optional<Budget> existingBudget =
        budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            user, request.getCategory(), request.getStartDate(), request.getEndDate());

    if (existingBudget.isPresent()) {
      throw new CustomException(
          HttpStatus.BAD_REQUEST,
          "sm.budget.fail.duplicate_period",
          "같은 카테고리의 예산이 해당 기간에 이미 존재합니다");
    }
  }

  /**
   * 사용자 ID로 사용자를 찾습니다.
   */
  private User findUserByUserId(String userId) {
    return userRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "sm.common.fail.user_not_found",
                    "올바르지 않은 사용자 정보입니다."));
  }

  /**
   * 예산 ID로 예산을 찾습니다.
   */
  private Budget findBudgetById(Long budgetId) {
    return budgetRepository
        .findById(budgetId)
        .orElseThrow(
            () ->
                new CustomException(
                    HttpStatus.NOT_FOUND, "sm.budget.fail.not_found", "예산을 찾을 수 없습니다"));
  }

  /**
   * 예산의 소유자를 검증합니다.
   */
  private void validateBudgetOwnership(String userId, Budget budget) {
    if (!budget.getUser().getUserId().equals(userId)) {
      throw new CustomException(
          HttpStatus.FORBIDDEN,
          "sm.budget.fail.access_denied",
          "본인의 예산만 접근할 수 있습니다");
    }
  }
}
