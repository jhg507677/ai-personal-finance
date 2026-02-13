package com.codingcat.aipersonalfinance.api.service.ledger;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.domain.activity.Category;
import com.codingcat.aipersonalfinance.domain.activity.LedgerType;
import com.codingcat.aipersonalfinance.domain.activity.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public class AddLedgerRequest extends BaseEntity {
  private LedgerType type; // INCOME, EXPENSE

  @PositiveOrZero
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 255)
  private String description; // 사용내역

  @Column(length = 100)
  private String place; // 사용처

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Category category; // 식비, 교통비 등

//  @Schema(description = "마지막 글쓴이, 현재 버전은 1명만 이용 가능")
//  private User author;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod; // 카드, 현금, 계좌이체

  @PastOrPresent
  @Column(name = "recorded_date", nullable = false)
  private LocalDate recordedDate;
}