package com.codingcat.aipersonalfinance.domain.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출 트렌드 분석 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendResponse {

    @Schema(description = "당월 통계")
    private MonthlyStatsResponse currentMonth;

    @Schema(description = "전월 통계")
    private MonthlyStatsResponse previousMonth;

    @Schema(description = "지출 증감율 (%)")
    private BigDecimal expenseChangeRate;

    @Schema(description = "수입 증감율 (%)")
    private BigDecimal incomeChangeRate;
}
