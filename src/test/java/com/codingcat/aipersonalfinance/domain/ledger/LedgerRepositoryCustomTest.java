package com.codingcat.aipersonalfinance.domain.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.config.QueryDslConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * LedgerRepositoryCustom 테스트
 * QueryDSL을 사용한 통계 쿼리 테스트
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LedgerRepositoryCustomTest {

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.createTestUser());
        otherUser = userRepository.save(User.createTestOtherUser());
    }

    @Nested
    @DisplayName("월별 요약 통계 조회")
    class GetMonthlySummaryTest {

        @Test
        @DisplayName("월별 수입/지출 합계를 정확히 계산한다")
        void getMonthlySummary_Success() {
            // Given: 2026년 1월과 2월 데이터
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2026, 1, 25));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, LocalDate.of(2026, 1, 15));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("1000000"),
                    Category.LIVING, LocalDate.of(2026, 1, 1));

            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2026, 2, 25));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("300000"),
                    Category.FOOD, LocalDate.of(2026, 2, 10));

            // When
            List<MonthlySummary> result = ledgerRepository.getMonthlySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 2, 28)
            );

            // Then
            assertThat(result).hasSize(2);

            // 1월 검증
            MonthlySummary jan = result.get(0);
            assertThat(jan.getYear()).isEqualTo(2026);
            assertThat(jan.getMonth()).isEqualTo(1);
            assertThat(jan.getTotalIncome()).isEqualByComparingTo(new BigDecimal("3000000"));
            assertThat(jan.getTotalExpense()).isEqualByComparingTo(new BigDecimal("1500000"));
            assertThat(jan.getNetAmount()).isEqualByComparingTo(new BigDecimal("1500000"));

            // 2월 검증
            MonthlySummary feb = result.get(1);
            assertThat(feb.getYear()).isEqualTo(2026);
            assertThat(feb.getMonth()).isEqualTo(2);
            assertThat(feb.getTotalIncome()).isEqualByComparingTo(new BigDecimal("3000000"));
            assertThat(feb.getTotalExpense()).isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(feb.getNetAmount()).isEqualByComparingTo(new BigDecimal("2700000"));
        }

        @Test
        @DisplayName("다른 사용자의 데이터는 포함하지 않는다")
        void getMonthlySummary_FiltersByUser() {
            // Given
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2026, 1, 25));
            createLedger(otherUser, LedgerType.INCOME, new BigDecimal("5000000"),
                    Category.ETC, LocalDate.of(2026, 1, 25));

            // When
            List<MonthlySummary> result = ledgerRepository.getMonthlySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTotalIncome()).isEqualByComparingTo(new BigDecimal("3000000"));
        }

        @Test
        @DisplayName("기간 외 데이터는 제외된다")
        void getMonthlySummary_FiltersByDateRange() {
            // Given
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2025, 12, 25)); // 2025년 12월
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2026, 1, 25)); // 2026년 1월

            // When: 2026년 1월만 조회
            List<MonthlySummary> result = ledgerRepository.getMonthlySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getYear()).isEqualTo(2026);
            assertThat(result.get(0).getMonth()).isEqualTo(1);
        }

        @Test
        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        void getMonthlySummary_ReturnsEmptyList_WhenNoData() {
            // When
            List<MonthlySummary> result = ledgerRepository.getMonthlySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리별 지출 요약 조회")
    class GetCategorySummaryTest {

        @Test
        @DisplayName("카테고리별 지출 합계와 건수를 계산한다")
        void getCategorySummary_Success() {
            // Given
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, LocalDate.of(2026, 1, 15));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("300000"),
                    Category.FOOD, LocalDate.of(2026, 1, 20));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("1000000"),
                    Category.LIVING, LocalDate.of(2026, 1, 1));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("200000"),
                    Category.TRANSPORT, LocalDate.of(2026, 1, 10));

            // When
            List<CategorySummary> result = ledgerRepository.getCategorySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(3);

            // 지출 많은 순으로 정렬됨
            CategorySummary living = result.get(0);
            assertThat(living.getCategory()).isEqualTo(Category.LIVING);
            assertThat(living.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
            assertThat(living.getTransactionCount()).isEqualTo(1);

            CategorySummary food = result.get(1);
            assertThat(food.getCategory()).isEqualTo(Category.FOOD);
            assertThat(food.getTotalAmount()).isEqualByComparingTo(new BigDecimal("800000"));
            assertThat(food.getTransactionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("수입(INCOME)은 제외하고 지출(EXPENSE)만 집계한다")
        void getCategorySummary_OnlyExpenses() {
            // Given
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, LocalDate.of(2026, 1, 25));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, LocalDate.of(2026, 1, 15));

            // When
            List<CategorySummary> result = ledgerRepository.getCategorySummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo(Category.FOOD);
        }
    }

    @Nested
    @DisplayName("결제수단별 통계 조회")
    class GetPaymentMethodSummaryTest {

        @Test
        @DisplayName("결제수단별 합계와 건수를 계산한다")
        void getPaymentMethodSummary_Success() {
            // Given
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, PaymentMethod.CARD, LocalDate.of(2026, 1, 15));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("300000"),
                    Category.FOOD, PaymentMethod.CARD, LocalDate.of(2026, 1, 20));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("100000"),
                    Category.TRANSPORT, PaymentMethod.CASH, LocalDate.of(2026, 1, 10));

            // When
            List<PaymentMethodSummary> result = ledgerRepository.getPaymentMethodSummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(2);

            PaymentMethodSummary card = result.get(0);
            assertThat(card.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(card.getTotalAmount()).isEqualByComparingTo(new BigDecimal("800000"));
            assertThat(card.getTransactionCount()).isEqualTo(2);

            PaymentMethodSummary cash = result.get(1);
            assertThat(cash.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
            assertThat(cash.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(cash.getTransactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("수입과 지출을 모두 포함한다")
        void getPaymentMethodSummary_IncludesBothTypes() {
            // Given
            createLedger(testUser, LedgerType.INCOME, new BigDecimal("3000000"),
                    Category.ETC, PaymentMethod.TRANSFER, LocalDate.of(2026, 1, 25));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, PaymentMethod.CARD, LocalDate.of(2026, 1, 15));

            // When
            List<PaymentMethodSummary> result = ledgerRepository.getPaymentMethodSummary(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31)
            );

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Top N 카테고리 조회")
    class GetTopCategoriesTest {

        @Test
        @DisplayName("지출이 많은 상위 N개 카테고리를 조회한다")
        void getTopCategories_Success() {
            // Given: 4개 카테고리 생성
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("1000000"),
                    Category.LIVING, LocalDate.of(2026, 1, 1));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("800000"),
                    Category.FOOD, LocalDate.of(2026, 1, 15));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("200000"),
                    Category.TRANSPORT, LocalDate.of(2026, 1, 10));
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("100000"),
                    Category.CAFE, LocalDate.of(2026, 1, 12));

            // When: Top 3 조회
            List<CategorySummary> result = ledgerRepository.getTopCategories(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31),
                    3
            );

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getCategory()).isEqualTo(Category.LIVING);
            assertThat(result.get(1).getCategory()).isEqualTo(Category.FOOD);
            assertThat(result.get(2).getCategory()).isEqualTo(Category.TRANSPORT);
        }

        @Test
        @DisplayName("limit보다 데이터가 적으면 있는 만큼만 반환한다")
        void getTopCategories_LessThanLimit() {
            // Given
            createLedger(testUser, LedgerType.EXPENSE, new BigDecimal("500000"),
                    Category.FOOD, LocalDate.of(2026, 1, 15));

            // When: Top 5 조회 (실제로는 1개만 있음)
            List<CategorySummary> result = ledgerRepository.getTopCategories(
                    testUser,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 31),
                    5
            );

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // === Helper Methods ===

    private void createLedger(User user, LedgerType type, BigDecimal amount,
                              Category category, LocalDate recordedDate) {
        createLedger(user, type, amount, category, PaymentMethod.CARD, recordedDate);
    }

    private void createLedger(User user, LedgerType type, BigDecimal amount,
                              Category category, PaymentMethod paymentMethod, LocalDate recordedDate) {
        Ledger ledger = Ledger.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .desc("Test Transaction")
                .place("Test Place")
                .category(category)
                .paymentMethod(paymentMethod)
                .recordedDate(recordedDate)
                .isAutoGenerated(false)
                .build();
        ledgerRepository.save(ledger);
    }
}
