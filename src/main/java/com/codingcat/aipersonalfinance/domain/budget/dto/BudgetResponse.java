package com.codingcat.aipersonalfinance.domain.budget.dto;

import com.codingcat.aipersonalfinance.domain.budget.Budget;
import com.codingcat.aipersonalfinance.domain.budget.BudgetPeriod;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
  private Long budgetIdx;
  private String name;
  private BudgetPeriod budgetPeriod;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal amount;
  private Category category;
  private Boolean isActive;
  private BigDecimal alertThreshold;
  private Boolean isAlertSent;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  /**
   * Budget 엔티티를 BudgetResponse DTO로 변환합니다.
   */
  public static BudgetResponse from(Budget budget) {
    return BudgetResponse.builder()
        .budgetIdx(budget.getIdx())
        .name(budget.getName())
        .budgetPeriod(budget.getBudgetPeriod())
        .startDate(budget.getStartDate())
        .endDate(budget.getEndDate())
        .amount(budget.getAmount())
        .category(budget.getCategory())
        .isActive(budget.getIsActive())
        .alertThreshold(budget.getAlertThreshold())
        .isAlertSent(budget.getIsAlertSent())
        .createdAt(budget.getCreatedAt())
        .modifiedAt(budget.getModifiedAt())
        .build();
  }
}
