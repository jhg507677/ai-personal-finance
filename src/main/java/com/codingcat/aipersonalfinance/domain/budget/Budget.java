package com.codingcat.aipersonalfinance.domain.budget;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetUpdateRequest;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 예산 엔티티
 * 사용자가 설정한 예산 정보를 관리합니다.
 */
@Entity
@Table(
    name = "budget",
    indexes = {
      @Index(name = "idx_budget_user_period", columnList = "user_idx, start_date, end_date"),
      @Index(name = "idx_budget_category", columnList = "category")
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Budget extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "budget_idx")
  private Long idx;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_idx", nullable = false)
  @Schema(description = "예산 소유자")
  private User user;

  @Column(nullable = false, length = 100)
  @Schema(description = "예산 이름 (예: 2024년 1월 식비 예산)")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Schema(description = "예산 주기: WEEKLY, MONTHLY, YEARLY")
  private BudgetPeriod budgetPeriod;

  @Column(nullable = false)
  @Schema(description = "예산 시작일")
  private LocalDate startDate;

  @Column(nullable = false)
  @Schema(description = "예산 종료일")
  private LocalDate endDate;

  @PositiveOrZero
  @Column(nullable = false, precision = 15, scale = 2)
  @Schema(description = "예산 금액")
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  @Schema(description = "특정 카테고리 예산 (null이면 전체 예산)")
  private Category category;

  @Column(name = "is_active", nullable = false)
  @Schema(description = "활성화 여부")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "alert_threshold", precision = 5, scale = 2)
  @Schema(description = "알림 임계값 (퍼센테이지, 예: 80.00 = 80%)")
  @Builder.Default
  private BigDecimal alertThreshold = new BigDecimal("80.00");

  @Column(name = "is_alert_sent", nullable = false)
  @Schema(description = "알림 발송 여부 (이벤트 중복 방지)")
  @Builder.Default
  private Boolean isAlertSent = false;

  /**
   * 예산 기간이 만료되었는지 확인합니다.
   *
   * @return 만료된 경우 true
   */
  public boolean isExpired() {
    return LocalDate.now().isAfter(endDate);
  }

  /**
   * 특정 날짜가 예산 기간 내에 있는지 확인합니다.
   *
   * @param date 확인할 날짜
   * @return 예산 기간 내에 있으면 true
   */
  public boolean isWithinPeriod(LocalDate date) {
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * 예산을 비활성화합니다.
   */
  public void deactivate() {
    this.isActive = false;
  }

  /**
   * 알림이 발송되었음을 표시합니다.
   */
  public void markAlertSent() {
    this.isAlertSent = true;
  }

  /**
   * 알림 플래그를 리셋합니다.
   */
  public void resetAlert() {
    this.isAlertSent = false;
  }

  /**
   * 예산을 수정합니다.
   */
  public void update(BudgetUpdateRequest request) {
    this.name = request.getName();
    this.budgetPeriod = request.getBudgetPeriod();
    this.startDate = request.getStartDate();
    this.endDate = request.getEndDate();
    this.amount = request.getAmount();
    this.category = request.getCategory();
    this.alertThreshold = request.getAlertThreshold();
    this.isActive = request.getIsActive();
  }
}
