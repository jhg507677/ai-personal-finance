package com.codingcat.aipersonalfinance.domain.memo;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Entity
@Table(
  name = "memo",
  // 유니크 인덱스/제약조건을 생성
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_memo_date", columnNames = "memo_date")
  }
)
public class Memo extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idx;

  @Column(nullable = false, length = 200)
  private String content;

  @PastOrPresent
  @Column(name = "recorded_date", nullable = false, unique = true)
  private LocalDate recordedDate;
}