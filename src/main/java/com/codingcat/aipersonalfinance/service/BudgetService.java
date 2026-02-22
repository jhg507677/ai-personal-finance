package com.codingcat.aipersonalfinance.service;

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
import com.codingcat.aipersonalfinance.exception.BusinessException;
import com.codingcat.aipersonalfinance.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

  /**
   * 예산을 생성합니다.
   *
   * @param userId 사용자 ID
   * @param request 예산 생성 요청 DTO
   * @return 생성된 예산 응답 DTO
   * @throws BusinessException 사용자를 찾을 수 없거나 기간이 겹치는 경우
   */
  @Transactional
  public BudgetResponse createBudget(String userId, BudgetCreateRequest request) {
    User user = findUserByUserId(userId);

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
    return BudgetResponse.from(savedBudget);
  }

  /**
   * 예산 상세 정보를 조회합니다.
   *
   * @param userId 사용자 ID
   * @param budgetId 예산 ID
   * @return 예산 응답 DTO
   * @throws BusinessException 예산을 찾을 수 없거나 접근 권한이 없는 경우
   */
  public BudgetResponse getBudget(String userId, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(userId, budget);
    return BudgetResponse.from(budget);
  }

  /**
   * 예산 사용 현황을 조회합니다.
   *
   * @param userId 사용자 ID
   * @param budgetId 예산 ID
   * @return 예산 사용 현황 응답 DTO
   * @throws BusinessException 예산을 찾을 수 없거나 접근 권한이 없는 경우
   */
  public BudgetUsageResponse getBudgetUsage(String userId, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(userId, budget);

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

    return BudgetUsageResponse.builder()
        .budget(BudgetResponse.from(budget))
        .totalSpent(totalSpent)
        .remainingAmount(remainingAmount)
        .usagePercentage(usagePercentage)
        .isExceeded(isExceeded)
        .shouldAlert(shouldAlert)
        .build();
  }

  /**
   * 예산을 수정합니다.
   *
   * @param userId 사용자 ID
   * @param budgetId 예산 ID
   * @param request 예산 수정 요청 DTO
   * @return 수정된 예산 응답 DTO
   * @throws BusinessException 예산을 찾을 수 없거나 접근 권한이 없는 경우
   */
  @Transactional
  public BudgetResponse updateBudget(String userId, Long budgetId, BudgetUpdateRequest request) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(userId, budget);

    budget.update(
        request.getName(),
        request.getBudgetPeriod(),
        request.getStartDate(),
        request.getEndDate(),
        request.getAmount(),
        request.getCategory(),
        request.getAlertThreshold(),
        request.getIsActive());

    return BudgetResponse.from(budget);
  }

  /**
   * 예산을 삭제합니다 (Soft Delete).
   *
   * @param userId 사용자 ID
   * @param budgetId 예산 ID
   * @throws BusinessException 예산을 찾을 수 없거나 접근 권한이 없는 경우
   */
  @Transactional
  public void deleteBudget(String userId, Long budgetId) {
    Budget budget = findBudgetById(budgetId);
    validateBudgetOwnership(userId, budget);
    budget.softDelete();
  }

  /**
   * 사용자의 활성화된 예산 목록을 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 예산 목록
   */
  public List<BudgetResponse> getBudgetList(String userId) {
    User user = findUserByUserId(userId);
    List<Budget> budgets = budgetRepository.findByUserAndIsActiveTrue(user);
    return budgets.stream().map(BudgetResponse::from).collect(Collectors.toList());
  }

  /**
   * 기간 중복 검증
   */
  private void validateDuplicatePeriod(User user, BudgetCreateRequest request) {
    Optional<Budget> existingBudget =
        budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            user, request.getCategory(), request.getStartDate(), request.getEndDate());

    if (existingBudget.isPresent()) {
      throw new BusinessException(
          ErrorCode.INVALID_INPUT_VALUE, "같은 카테고리의 예산이 해당 기간에 이미 존재합니다");
    }
  }

  /**
   * 사용자 ID로 사용자를 찾습니다.
   */
  private User findUserByUserId(String userId) {
    return userRepository
        .findByUserId(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  /**
   * 예산 ID로 예산을 찾습니다.
   */
  private Budget findBudgetById(Long budgetId) {
    return budgetRepository
        .findById(budgetId)
        .orElseThrow(
            () -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "예산을 찾을 수 없습니다"));
  }

  /**
   * 예산의 소유자를 검증합니다.
   */
  private void validateBudgetOwnership(String userId, Budget budget) {
    if (!budget.getUser().getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 예산만 접근할 수 있습니다");
    }
  }
}
