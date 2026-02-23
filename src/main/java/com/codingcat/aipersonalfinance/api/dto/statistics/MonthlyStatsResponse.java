package com.codingcat.aipersonalfinance.api.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatsResponse {

    @Schema(description = "년도")
    private Integer year;

    @Schema(description = "월")
    private Integer month;

    @Schema(description = "총 수입")
    private BigDecimal totalIncome;

    @Schema(description = "총 지출")
    private BigDecimal totalExpense;

    @Schema(description = "순액 (수입 - 지출)")
    private BigDecimal netAmount;
}
