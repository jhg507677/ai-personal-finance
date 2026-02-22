package com.codingcat.aipersonalfinance.api.dto.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 내역 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerCreateRequest {

  @NotNull(message = "거래 유형은 필수입니다")
  private LedgerType type;

  @NotNull(message = "거래 금액은 필수입니다")
  @Positive(message = "거래 금액은 0보다 커야 합니다")
  private BigDecimal amount;

  @NotNull(message = "거래 설명은 필수입니다")
  @Size(min = 1, max = 255, message = "거래 설명은 1자 이상 255자 이하여야 합니다")
  private String desc;

  @Size(max = 100, message = "거래 장소는 100자 이하여야 합니다")
  private String place;

  @NotNull(message = "카테고리는 필수입니다")
  private Category category;

  @NotNull(message = "결제 수단은 필수입니다")
  private PaymentMethod paymentMethod;

  @NotNull(message = "거래 날짜는 필수입니다")
  private LocalDate recordedDate;
}
