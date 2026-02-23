package com.codingcat.aipersonalfinance.domain.budget.dto;

import com.codingcat.aipersonalfinance.domain.budget.BudgetPeriod;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUpdateRequest {

  @NotNull(message = "예산 이름은 필수입니다")
  @Size(min = 1, max = 100, message = "예산 이름은 1자 이상 100자 이하여야 합니다")
  private String name;

  @NotNull(message = "예산 주기는 필수입니다")
  private BudgetPeriod budgetPeriod;

  @NotNull(message = "시작 날짜는 필수입니다")
  private LocalDate startDate;

  @NotNull(message = "종료 날짜는 필수입니다")
  private LocalDate endDate;

  @NotNull(message = "예산 금액은 필수입니다")
  @Positive(message = "예산 금액은 0보다 커야 합니다")
  private BigDecimal amount;

  private Category category; // 선택사항 (null이면 전체 예산)

  @Positive(message = "알림 임계값은 0보다 커야 합니다")
  private BigDecimal alertThreshold; // 선택사항 (기본값 80.00)

  @NotNull(message = "활성 상태는 필수입니다")
  private Boolean isActive;
}
