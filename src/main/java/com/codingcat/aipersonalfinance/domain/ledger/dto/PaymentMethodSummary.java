package com.codingcat.aipersonalfinance.domain.ledger.dto;

import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제수단별 통계 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodSummary {
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private Long transactionCount;
}
