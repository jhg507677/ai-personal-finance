package com.codingcat.aipersonalfinance.domain.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import java.time.LocalDate;
import java.util.List;

/**
 * 통계 조회를 위한 커스텀 Repository 인터페이스
 */
public interface LedgerRepositoryCustom {

    /**
     * 사용자의 월별 수입/지출 요약 조회
     *
     * @param user 사용자
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 월별 요약 목록
     */
    List<MonthlySummary> getMonthlySummary(User user, LocalDate startDate, LocalDate endDate);

    /**
     * 사용자의 카테고리별 지출 요약 조회
     *
     * @param user 사용자
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 카테고리별 요약 목록
     */
    List<CategorySummary> getCategorySummary(User user, LocalDate startDate, LocalDate endDate);

    /**
     * 사용자의 결제수단별 통계 조회
     *
     * @param user 사용자
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 결제수단별 요약 목록
     */
    List<PaymentMethodSummary> getPaymentMethodSummary(User user, LocalDate startDate, LocalDate endDate);

    /**
     * 카테고리별 지출 Top N 조회
     *
     * @param user 사용자
     * @param startDate 시작일
     * @param endDate 종료일
     * @param limit 조회 개수
     * @return 카테고리별 요약 목록 (지출 많은 순)
     */
    List<CategorySummary> getTopCategories(User user, LocalDate startDate, LocalDate endDate, int limit);
}
