package com.codingcat.aipersonalfinance.domain.ledger.dto;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 내역 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerUpdateRequest {

  private LedgerType type;

  @Positive(message = "거래 금액은 0보다 커야 합니다")
  private BigDecimal amount;

  @Size(min = 1, max = 255, message = "거래 설명은 1자 이상 255자 이하여야 합니다")
  private String desc;

  @Size(max = 100, message = "거래 장소는 100자 이하여야 합니다")
  private String place;

  private Category category;

  private PaymentMethod paymentMethod;

  private LocalDate recordedDate;
}
