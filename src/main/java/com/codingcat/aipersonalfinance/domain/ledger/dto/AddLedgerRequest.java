package com.codingcat.aipersonalfinance.domain.ledger.dto;

import static com.codingcat.aipersonalfinance.domain.ledger.Category.ETC;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.Ledger;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class AddLedgerRequest extends BaseEntity {

  @NotNull(message = "필수 값이 누락되었습니다.")
  private LedgerType type;

  @NotNull(message = "금액을 필수로 입력해주세요.")
  @Positive(message = "금액은 0보다 커야 합니다.")
  private BigDecimal amount;
  private String desc;
  private String place;
  private Category category;

  @NotNull(message = "필수 값이 누락되었습니다.")
  private PaymentMethod paymentMethod; // 카드, 현금, 계좌이체

  private LocalDate recordedDate;

  public Ledger toEntity() {
    return Ledger.builder()
      .type(this.type)
      .amount(this.amount)
      .desc(this.desc)
      .place(this.place)
      .category(this.category != null ? this.category : ETC)
      .paymentMethod(this.paymentMethod)
      .recordedDate(this.recordedDate != null ? this.recordedDate : LocalDate.now())
      .build();
  }
}