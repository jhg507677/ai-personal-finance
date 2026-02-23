package com.codingcat.aipersonalfinance.api.dto.statistics;

import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제수단별 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodStatsResponse {

    @Schema(description = "결제수단")
    private PaymentMethod paymentMethod;

    @Schema(description = "총 금액")
    private BigDecimal totalAmount;

    @Schema(description = "거래 건수")
    private Long transactionCount;
}
