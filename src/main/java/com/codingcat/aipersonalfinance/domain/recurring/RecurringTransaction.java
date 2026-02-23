package com.codingcat.aipersonalfinance.domain.recurring;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.Ledger;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import com.codingcat.aipersonalfinance.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 정기 거래 엔티티
 * 정기적으로 발생하는 수입/지출 정보를 관리합니다.
 */
@Entity
@Table(
    name = "recurring_transaction",
    indexes = {
      @Index(name = "idx_recurring_user", columnList = "user_idx"),
      @Index(name = "idx_recurring_next_execution", columnList = "next_execution_date, is_active")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class RecurringTransaction extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recurring_transaction_idx")
  private Long idx;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_idx", nullable = false)
  @Schema(description = "정기 거래 소유자")
  private User user;

  @Column(nullable = false, length = 100)
  @Schema(description = "정기 거래 이름 (예: 월세, Netflix 구독)")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Schema(description = "INCOME: 수입, EXPENSE: 지출")
  private LedgerType type;

  @PositiveOrZero
  @Column(nullable = false, precision = 15, scale = 2)
  @Schema(description = "거래 금액")
  private BigDecimal amount;

  @Column(nullable = false, length = 255)
  @Schema(description = "거래 설명")
  private String description;

  @Column(length = 100)
  @Schema(description = "거래 장소/대상")
  private String place;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Schema(description = "카테고리")
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 50)
  @Schema(description = "결제 수단")
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence_pattern", nullable = false, length = 20)
  @Schema(description = "반복 주기: DAILY, WEEKLY, MONTHLY, YEARLY")
  private RecurrencePattern recurrencePattern;

  @Column(name = "recurrence_interval", nullable = false)
  @Schema(description = "반복 간격 (예: 2주마다 = pattern:WEEKLY, interval:2)")
  @Builder.Default
  private Integer recurrenceInterval = 1;

  @Column(name = "start_date", nullable = false)
  @Schema(description = "정기 거래 시작일")
  private LocalDate startDate;

  @Column(name = "end_date")
  @Schema(description = "정기 거래 종료일 (null이면 무기한)")
  private LocalDate endDate;

  @Column(name = "next_execution_date", nullable = false)
  @Schema(description = "다음 실행 예정일")
  private LocalDate nextExecutionDate;

  @Column(name = "last_execution_date")
  @Schema(description = "마지막 실행일")
  private LocalDate lastExecutionDate;

  @Column(name = "is_active", nullable = false)
  @Schema(description = "활성화 여부")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "execution_day_of_month")
  @Schema(description = "매월 실행 일자 (1-31, MONTHLY 패턴에서 사용)")
  private Integer executionDayOfMonth;

  @OneToMany(mappedBy = "recurringTransaction", cascade = CascadeType.ALL)
  @Schema(description = "이 정기 거래로 생성된 가계부 기록들")
  @Builder.Default
  private List<Ledger> generatedLedgers = new ArrayList<>();

  /**
   * 정기 거래가 만료되었는지 확인합니다.
   *
   * @return 만료된 경우 true (무기한인 경우 항상 false)
   */
  public boolean isExpired() {
    if (endDate == null) {
      return false; // 무기한 정기 거래
    }
    return LocalDate.now().isAfter(endDate);
  }

  /**
   * 오늘 실행해야 하는지 확인합니다.
   *
   * @return 오늘 실행해야 하면 true
   */
  public boolean shouldExecuteToday() {
    LocalDate today = LocalDate.now();
    return isActive && !isExpired() && today.equals(nextExecutionDate);
  }

  /**
   * 다음 실행일을 계산합니다.
   */
  public void calculateNextExecutionDate() {
    LocalDate base = (lastExecutionDate != null) ? lastExecutionDate : startDate;

    switch (recurrencePattern) {
      case DAILY:
        this.nextExecutionDate = base.plusDays(recurrenceInterval);
        break;
      case WEEKLY:
        this.nextExecutionDate = base.plusWeeks(recurrenceInterval);
        break;
      case MONTHLY:
        LocalDate nextMonth = base.plusMonths(recurrenceInterval);
        if (executionDayOfMonth != null) {
          int maxDay = nextMonth.lengthOfMonth();
          int day = Math.min(executionDayOfMonth, maxDay);
          this.nextExecutionDate = nextMonth.withDayOfMonth(day);
        } else {
          this.nextExecutionDate = nextMonth;
        }
        break;
      case YEARLY:
        this.nextExecutionDate = base.plusYears(recurrenceInterval);
        break;
    }
  }

  /**
   * 실행 완료 표시 및 다음 실행일 계산을 수행합니다.
   *
   * @param executionDate 실행일
   */
  public void markExecuted(LocalDate executionDate) {
    this.lastExecutionDate = executionDate;
    calculateNextExecutionDate();
  }

  /**
   * 정기 거래를 비활성화합니다.
   */
  public void deactivate() {
    this.isActive = false;
  }

  /**
   * 정기 거래를 활성화합니다.
   */
  public void activate() {
    this.isActive = true;
  }

  /**
   * 정기 거래로부터 Ledger 엔티티를 생성합니다.
   *
   * @param recordedDate 기록 날짜
   * @return 생성된 Ledger 엔티티
   */
  public Ledger createLedgerFromRecurring(LocalDate recordedDate) {
    return Ledger.builder()
        .user(this.user)
        .type(this.type)
        .amount(this.amount)
        .desc(this.description)
        .place(this.place)
        .category(this.category)
        .paymentMethod(this.paymentMethod)
        .recordedDate(recordedDate)
        .isAutoGenerated(true)
        .recurringTransaction(this)
        .build();
  }
}
