package com.codingcat.aipersonalfinance.domain.budget.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예산 사용 현황 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUsageResponse {

  private BudgetResponse budget; // 예산 정보

  private BigDecimal totalSpent; // 실제 지출 금액

  private BigDecimal remainingAmount; // 남은 예산

  private BigDecimal usagePercentage; // 사용률 (%)

  private Boolean isExceeded; // 예산 초과 여부

  private Boolean shouldAlert; // 알림 발송 필요 여부
}
