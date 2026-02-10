package com.codingcat.aipersonalfinance.domain.activity;

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
@Table(name = "activity")
public class Activity extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idx;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ActivityType type; // INCOME, EXPENSE

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

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  private PaymentMethod paymentMethod; // 카드, 현금, 계좌이체

  @PastOrPresent
  @Column(name = "recorded_date", nullable = false)
  private LocalDate recordedDate;
}