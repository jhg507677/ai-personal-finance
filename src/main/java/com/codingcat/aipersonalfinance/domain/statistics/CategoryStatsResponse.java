package com.codingcat.aipersonalfinance.domain.statistics;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리별 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatsResponse {

    @Schema(description = "카테고리")
    private Category category;

    @Schema(description = "총 금액")
    private BigDecimal totalAmount;

    @Schema(description = "거래 건수")
    private Long transactionCount;

    @Schema(description = "전체 대비 비율 (%)")
    private BigDecimal percentage;
}
