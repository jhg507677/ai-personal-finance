package com.codingcat.aipersonalfinance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codingcat.aipersonalfinance.api.dto.statistics.CategoryStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.MonthlyStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.PaymentMethodStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.TrendResponse;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerRepository;
import com.codingcat.aipersonalfinance.domain.ledger.PaymentMethod;
import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * StatisticsService 테스트
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.createTestUser();
    }

    @Nested
    @DisplayName("월별 통계 조회")
    class GetMonthlyStatisticsTest {

        @Test
        @DisplayName("월별 수입/지출 통계를 조회한다")
        void getMonthlyStatistics_Success() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 2, 28);

            List<MonthlySummary> summaries = Arrays.asList(
                    MonthlySummary.builder()
                            .year(2026)
                            .month(1)
                            .totalIncome(new BigDecimal("3000000"))
                            .totalExpense(new BigDecimal("1500000"))
                            .netAmount(new BigDecimal("1500000"))
                            .build(),
                    MonthlySummary.builder()
                            .year(2026)
                            .month(2)
                            .totalIncome(new BigDecimal("3000000"))
                            .totalExpense(new BigDecimal("2000000"))
                            .netAmount(new BigDecimal("1000000"))
                            .build()
            );

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getMonthlySummary(eq(testUser), eq(startDate), eq(endDate)))
                    .thenReturn(summaries);

            // When
            List<MonthlyStatsResponse> result = statisticsService.getMonthlyStatistics(
                    "testId", startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getYear()).isEqualTo(2026);
            assertThat(result.get(0).getMonth()).isEqualTo(1);
            assertThat(result.get(0).getTotalIncome()).isEqualByComparingTo(new BigDecimal("3000000"));

            verify(ledgerRepository).getMonthlySummary(testUser, startDate, endDate);
        }
    }

    @Nested
    @DisplayName("카테고리별 통계 조회")
    class GetCategoryStatisticsTest {

        @Test
        @DisplayName("카테고리별 지출 통계와 비율을 계산한다")
        void getCategoryStatistics_Success() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);

            List<CategorySummary> summaries = Arrays.asList(
                    CategorySummary.builder()
                            .category(Category.FOOD)
                            .totalAmount(new BigDecimal("600000"))
                            .transactionCount(10L)
                            .build(),
                    CategorySummary.builder()
                            .category(Category.TRANSPORT)
                            .totalAmount(new BigDecimal("400000"))
                            .transactionCount(5L)
                            .build()
            );

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getCategorySummary(eq(testUser), eq(startDate), eq(endDate)))
                    .thenReturn(summaries);

            // When
            List<CategoryStatsResponse> result = statisticsService.getCategoryStatistics(
                    "testId", startDate, endDate);

            // Then
            assertThat(result).hasSize(2);

            CategoryStatsResponse food = result.get(0);
            assertThat(food.getCategory()).isEqualTo(Category.FOOD);
            assertThat(food.getTotalAmount()).isEqualByComparingTo(new BigDecimal("600000"));
            assertThat(food.getTransactionCount()).isEqualTo(10L);
            assertThat(food.getPercentage()).isEqualByComparingTo(new BigDecimal("60.00"));

            CategoryStatsResponse transport = result.get(1);
            assertThat(transport.getPercentage()).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        void getCategoryStatistics_EmptyList() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getCategorySummary(eq(testUser), eq(startDate), eq(endDate)))
                    .thenReturn(Arrays.asList());

            // When
            List<CategoryStatsResponse> result = statisticsService.getCategoryStatistics(
                    "testId", startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("결제수단별 통계 조회")
    class GetPaymentMethodStatisticsTest {

        @Test
        @DisplayName("결제수단별 통계를 조회한다")
        void getPaymentMethodStatistics_Success() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);

            List<PaymentMethodSummary> summaries = Arrays.asList(
                    PaymentMethodSummary.builder()
                            .paymentMethod(PaymentMethod.CARD)
                            .totalAmount(new BigDecimal("800000"))
                            .transactionCount(15L)
                            .build(),
                    PaymentMethodSummary.builder()
                            .paymentMethod(PaymentMethod.CASH)
                            .totalAmount(new BigDecimal("200000"))
                            .transactionCount(5L)
                            .build()
            );

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getPaymentMethodSummary(eq(testUser), eq(startDate), eq(endDate)))
                    .thenReturn(summaries);

            // When
            List<PaymentMethodStatsResponse> result = statisticsService.getPaymentMethodStatistics(
                    "testId", startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo(new BigDecimal("800000"));
        }
    }

    @Nested
    @DisplayName("지출 트렌드 분석")
    class GetTrendAnalysisTest {

        @Test
        @DisplayName("전월 대비 지출 증감율을 계산한다")
        void getTrendAnalysis_Success() {
            // Given
            LocalDate currentMonth = LocalDate.of(2026, 2, 1);

            List<MonthlySummary> summaries = Arrays.asList(
                    // 전월 (1월)
                    MonthlySummary.builder()
                            .year(2026)
                            .month(1)
                            .totalIncome(new BigDecimal("3000000"))
                            .totalExpense(new BigDecimal("1000000"))
                            .netAmount(new BigDecimal("2000000"))
                            .build(),
                    // 당월 (2월)
                    MonthlySummary.builder()
                            .year(2026)
                            .month(2)
                            .totalIncome(new BigDecimal("3000000"))
                            .totalExpense(new BigDecimal("1500000"))
                            .netAmount(new BigDecimal("1500000"))
                            .build()
            );

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getMonthlySummary(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(summaries);

            // When
            TrendResponse result = statisticsService.getTrendAnalysis("testId", currentMonth);

            // Then
            assertThat(result.getCurrentMonth().getTotalExpense())
                    .isEqualByComparingTo(new BigDecimal("1500000"));
            assertThat(result.getPreviousMonth().getTotalExpense())
                    .isEqualByComparingTo(new BigDecimal("1000000"));
            // 증가율: (1500000 - 1000000) / 1000000 * 100 = 50%
            assertThat(result.getExpenseChangeRate()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.getIncomeChangeRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Top N 카테고리 조회")
    class GetTopCategoriesTest {

        @Test
        @DisplayName("지출이 많은 상위 카테고리를 조회한다")
        void getTopCategories_Success() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);
            int limit = 5;

            List<CategorySummary> summaries = Arrays.asList(
                    CategorySummary.builder()
                            .category(Category.FOOD)
                            .totalAmount(new BigDecimal("800000"))
                            .transactionCount(15L)
                            .build()
            );

            when(userRepository.findByUserId("testId")).thenReturn(Optional.of(testUser));
            when(ledgerRepository.getTopCategories(eq(testUser), eq(startDate), eq(endDate), eq(limit)))
                    .thenReturn(summaries);

            // When
            List<CategoryStatsResponse> result = statisticsService.getTopCategories(
                    "testId", startDate, endDate, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo(Category.FOOD);

            verify(ledgerRepository).getTopCategories(testUser, startDate, endDate, limit);
        }
    }
}
