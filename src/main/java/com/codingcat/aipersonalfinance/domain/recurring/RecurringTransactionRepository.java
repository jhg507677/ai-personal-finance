package com.codingcat.aipersonalfinance.domain.recurring;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.user.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * RecurringTransaction 엔티티에 대한 Repository
 */
@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

  /**
   * 사용자의 활성화된 정기 거래 목록을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 사용자
   * @return 활성화된 정기 거래 목록
   */
  List<RecurringTransaction> findByUserAndIsActiveTrue(User user);

  /**
   * 실행해야 할 정기 거래 목록을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param date 실행일
   * @return 실행해야 할 정기 거래 목록
   */
  @Query(
      "SELECT rt FROM RecurringTransaction rt WHERE rt.isActive = true "
          + "AND rt.nextExecutionDate <= :date "
          + "AND (rt.endDate IS NULL OR rt.endDate >= :date)")
  List<RecurringTransaction> findTransactionsToExecute(@Param("date") LocalDate date);

  /**
   * 사용자의 특정 카테고리 정기 거래 목록을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 사용자
   * @param category 카테고리
   * @return 해당 카테고리의 정기 거래 목록
   */
  List<RecurringTransaction> findByUserAndCategory(User user, Category category);
}
