package com.codingcat.aipersonalfinance.domain.ledger.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 수입/지출 요약 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummary {
    private Integer year;
    private Integer month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount; // 순액 (수입 - 지출)
}
