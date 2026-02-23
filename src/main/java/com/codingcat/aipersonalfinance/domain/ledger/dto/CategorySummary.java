package com.codingcat.aipersonalfinance.domain.ledger.dto;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리별 지출 요약 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummary {
    private Category category;
    private BigDecimal totalAmount;
    private Long transactionCount;
}
