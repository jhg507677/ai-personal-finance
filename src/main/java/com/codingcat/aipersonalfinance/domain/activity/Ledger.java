package com.codingcat.aipersonalfinance.domain.activity;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Entity
@Table(name = "ledger")
public class Ledger extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idx;

  @Schema(description = "INCOME: 수입, EXPENSE: 지출")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private LedgerType type;

  @PositiveOrZero
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Schema(description = "사용 내역")
  @Column(nullable = false, length = 255)
  private String desc;

  @Schema(description = "사용처")
  @Column(length = 100)
  private String place;

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