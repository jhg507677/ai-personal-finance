package com.codingcat.aipersonalfinance.api.service.ledger;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public class AddLedgerRequest extends BaseEntity {
  private LedgerType type;

  private BigDecimal amount;

  private String description;

  private String place;

  private Category category;

//  @Schema(description = "마지막 글쓴이, 현재 버전은 1명만 이용 가능")
//  private User author;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod; // 카드, 현금, 계좌이체

  @PastOrPresent
  @Column(name = "recorded_date", nullable = false)
  private LocalDate recordedDate;
}