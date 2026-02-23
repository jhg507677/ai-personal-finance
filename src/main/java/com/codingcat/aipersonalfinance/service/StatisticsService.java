package com.codingcat.aipersonalfinance.service;

import com.codingcat.aipersonalfinance.api.dto.statistics.CategoryStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.MonthlyStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.PaymentMethodStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.TrendResponse;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerRepository;
import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.exception.BusinessException;
import com.codingcat.aipersonalfinance.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    /**
     * 월별 통계 조회
     *
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 월별 통계 목록
     */
    public List<MonthlyStatsResponse> getMonthlyStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUserId(userId);

        List<MonthlySummary> summaries = ledgerRepository.getMonthlySummary(user, startDate, endDate);

        return summaries.stream()
                .map(summary -> MonthlyStatsResponse.builder()
                        .year(summary.getYear())
                        .month(summary.getMonth())
                        .totalIncome(summary.getTotalIncome())
                        .totalExpense(summary.getTotalExpense())
                        .netAmount(summary.getNetAmount())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 통계 조회 (비율 포함)
     *
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 카테고리별 통계 목록
     */
    public List<CategoryStatsResponse> getCategoryStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUserId(userId);

        List<CategorySummary> summaries = ledgerRepository.getCategorySummary(user, startDate, endDate);

        if (summaries.isEmpty()) {
            return List.of();
        }

        // 전체 지출 합계 계산
        BigDecimal totalExpense = summaries.stream()
                .map(CategorySummary::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 각 카테고리별 비율 계산
        return summaries.stream()
                .map(summary -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = summary.getTotalAmount()
                                .divide(totalExpense, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    return CategoryStatsResponse.builder()
                            .category(summary.getCategory())
                            .totalAmount(summary.getTotalAmount())
                            .transactionCount(summary.getTransactionCount())
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 결제수단별 통계 조회
     *
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 결제수단별 통계 목록
     */
    public List<PaymentMethodStatsResponse> getPaymentMethodStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUserId(userId);

        List<PaymentMethodSummary> summaries = ledgerRepository.getPaymentMethodSummary(user, startDate, endDate);

        return summaries.stream()
                .map(summary -> PaymentMethodStatsResponse.builder()
                        .paymentMethod(summary.getPaymentMethod())
                        .totalAmount(summary.getTotalAmount())
                        .transactionCount(summary.getTransactionCount())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 지출 트렌드 분석 (전월 대비)
     *
     * @param userId 사용자 ID
     * @param currentMonth 분석 대상 월 (해당 월의 아무 날짜)
     * @return 트렌드 분석 결과
     */
    public TrendResponse getTrendAnalysis(String userId, LocalDate currentMonth) {
        User user = findUserByUserId(userId);

        // 전월과 당월의 첫날/마지막날 계산
        LocalDate currentStart = currentMonth.withDayOfMonth(1);
        LocalDate currentEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());

        LocalDate previousStart = currentStart.minusMonths(1);
        LocalDate previousEnd = previousStart.withDayOfMonth(previousStart.lengthOfMonth());

        // 두 달의 통계 조회
        List<MonthlySummary> summaries = ledgerRepository.getMonthlySummary(
                user, previousStart, currentEnd);

        MonthlyStatsResponse currentStats = null;
        MonthlyStatsResponse previousStats = null;

        for (MonthlySummary summary : summaries) {
            MonthlyStatsResponse stats = MonthlyStatsResponse.builder()
                    .year(summary.getYear())
                    .month(summary.getMonth())
                    .totalIncome(summary.getTotalIncome())
                    .totalExpense(summary.getTotalExpense())
                    .netAmount(summary.getNetAmount())
                    .build();

            if (summary.getYear().equals(currentMonth.getYear()) &&
                    summary.getMonth().equals(currentMonth.getMonthValue())) {
                currentStats = stats;
            } else {
                previousStats = stats;
            }
        }

        // 기본값 설정 (데이터 없을 경우)
        if (currentStats == null) {
            currentStats = createEmptyStats(currentMonth.getYear(), currentMonth.getMonthValue());
        }
        if (previousStats == null) {
            previousStats = createEmptyStats(previousStart.getYear(), previousStart.getMonthValue());
        }

        // 증감율 계산
        BigDecimal expenseChangeRate = calculateChangeRate(
                previousStats.getTotalExpense(), currentStats.getTotalExpense());
        BigDecimal incomeChangeRate = calculateChangeRate(
                previousStats.getTotalIncome(), currentStats.getTotalIncome());

        return TrendResponse.builder()
                .currentMonth(currentStats)
                .previousMonth(previousStats)
                .expenseChangeRate(expenseChangeRate)
                .incomeChangeRate(incomeChangeRate)
                .build();
    }

    /**
     * Top N 카테고리 조회
     *
     * @param userId 사용자 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param limit 조회 개수
     * @return Top N 카테고리 목록
     */
    public List<CategoryStatsResponse> getTopCategories(String userId, LocalDate startDate, LocalDate endDate, int limit) {
        User user = findUserByUserId(userId);

        List<CategorySummary> summaries = ledgerRepository.getTopCategories(user, startDate, endDate, limit);

        // 전체 합계 계산 (비율 계산용)
        BigDecimal totalExpense = summaries.stream()
                .map(CategorySummary::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return summaries.stream()
                .map(summary -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = summary.getTotalAmount()
                                .divide(totalExpense, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    return CategoryStatsResponse.builder()
                            .category(summary.getCategory())
                            .totalAmount(summary.getTotalAmount())
                            .transactionCount(summary.getTransactionCount())
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // === Private Helper Methods ===

    private User findUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private MonthlyStatsResponse createEmptyStats(int year, int month) {
        return MonthlyStatsResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO)
                .build();
    }

    /**
     * 증감율 계산 ((현재 - 이전) / 이전 * 100)
     */
    private BigDecimal calculateChangeRate(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
