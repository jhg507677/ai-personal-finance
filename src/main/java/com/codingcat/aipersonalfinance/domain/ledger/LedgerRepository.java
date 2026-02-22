package com.codingcat.aipersonalfinance.domain.ledger;

import com.codingcat.aipersonalfinance.domain.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Ledger Repository
 * 거래 내역 조회 및 통계 기능을 제공합니다.
 */
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

  /**
   * 사용자의 활성 거래 내역을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @return 삭제되지 않은 거래 내역 목록
   */
  List<Ledger> findByUser(User user);

  /**
   * 사용자의 거래 내역을 날짜 범위로 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 기간 내 거래 내역 목록
   */
  List<Ledger> findByUserAndRecordedDateBetween(
      User user, LocalDate startDate, LocalDate endDate);

  /**
   * 사용자의 특정 카테고리 거래 내역을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @param category 카테고리
   * @return 해당 카테고리의 거래 내역 목록
   */
  List<Ledger> findByUserAndCategory(User user, Category category);

  /**
   * 사용자의 특정 거래 유형(수입/지출) 내역을 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @param type 거래 유형
   * @return 해당 유형의 거래 내역 목록
   */
  List<Ledger> findByUserAndType(User user, LedgerType type);

  /**
   * 사용자의 거래 내역을 카테고리와 날짜 범위로 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @param category 카테고리
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 조건에 맞는 거래 내역 목록
   */
  List<Ledger> findByUserAndCategoryAndRecordedDateBetween(
      User user, Category category, LocalDate startDate, LocalDate endDate);

  /**
   * 사용자의 거래 내역을 날짜 역순으로 정렬하여 조회합니다.
   * @SQLRestriction에 의해 삭제된 항목은 자동 제외됩니다.
   *
   * @param user 조회할 사용자
   * @return 날짜 역순으로 정렬된 거래 내역 목록
   */
  List<Ledger> findByUserOrderByRecordedDateDesc(User user);

  /**
   * 카테고리별 거래 합계를 계산합니다.
   *
   * @param user 조회할 사용자
   * @param type 거래 유형 (수입/지출)
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return [카테고리, 합계] 배열 목록
   */
  @Query(
      """
      SELECT l.category, SUM(l.amount)
      FROM Ledger l
      WHERE l.user = :user
        AND l.type = :type
        AND l.recordedDate BETWEEN :startDate AND :endDate
      GROUP BY l.category
      """)
  List<Object[]> calculateTotalByCategory(
      @Param("user") User user,
      @Param("type") LedgerType type,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  /**
   * 특정 기간의 거래 유형별 총 합계를 계산합니다.
   *
   * @param user 조회할 사용자
   * @param type 거래 유형 (수입/지출)
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 총 합계 (데이터 없으면 0)
   */
  @Query(
      """
      SELECT COALESCE(SUM(l.amount), 0)
      FROM Ledger l
      WHERE l.user = :user
        AND l.type = :type
        AND l.recordedDate BETWEEN :startDate AND :endDate
      """)
  BigDecimal calculateTotalByTypeAndDateRange(
      @Param("user") User user,
      @Param("type") LedgerType type,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
