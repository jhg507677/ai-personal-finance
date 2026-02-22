package com.codingcat.aipersonalfinance.api.dto.budget;

import com.codingcat.aipersonalfinance.domain.budget.BudgetPeriod;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
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

  @Size(min = 1, max = 100, message = "예산 이름은 1자 이상 100자 이하여야 합니다")
  private String name;

  private BudgetPeriod budgetPeriod;

  private LocalDate startDate;

  private LocalDate endDate;

  @Positive(message = "예산 금액은 0보다 커야 합니다")
  private BigDecimal amount;

  private Category category;

  private BigDecimal alertThreshold;

  private Boolean isActive;
}
