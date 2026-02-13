package com.codingcat.aipersonalfinance.domain.budget;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Budget 엔티티에 대한 Repository
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

  /**
   * 사용자의 활성화된 예산 목록을 조회합니다.
   *
   * @param user 사용자
   * @return 활성화된 예산 목록
   */
  List<Budget> findByUserAndIsActiveTrueAndDeletedAtIsNull(User user);

  /**
   * 특정 날짜에 해당하는 사용자의 예산을 조회합니다.
   *
   * @param user 사용자
   * @param date 조회할 날짜
   * @param sameDate 조회할 날짜 (endDate 비교용)
   * @return 해당 기간의 예산 목록
   */
  List<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndDeletedAtIsNull(
      User user, LocalDate date, LocalDate sameDate);

  /**
   * 특정 카테고리에 대한 특정 기간의 예산을 조회합니다.
   *
   * @param user 사용자
   * @param category 카테고리
   * @param startDate 시작일
   * @param endDate 종료일
   * @return 해당 조건의 예산
   */
  Optional<Budget>
      findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndDeletedAtIsNull(
          User user, Category category, LocalDate startDate, LocalDate endDate);

  /**
   * 알림 체크가 필요한 활성화된 예산 목록을 조회합니다.
   * @param today 오늘 날짜
   * @return 알림 체크가 필요한 예산 목록
   */
  @Query("""
    SELECT BUDGET FROM Budget AS BUDGET
    WHERE BUDGET.isActive = true AND BUDGET.isAlertSent = false
    AND BUDGET.startDate <= :today AND BUDGET.endDate >= :today
    AND BUDGET.deletedDateTime IS NULL
  """)
  List<Budget> findActiveBudgetsForAlertCheck(@Param("today") LocalDate today);
}
