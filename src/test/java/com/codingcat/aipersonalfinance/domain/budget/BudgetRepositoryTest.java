package com.codingcat.aipersonalfinance.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.codingcat.aipersonalfinance.config.TestJpaConfig;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.config.QueryDslConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * BudgetRepository 테스트
 */
@DisplayName("BudgetRepository 테스트")
@DataJpaTest
@Import({TestJpaConfig.class, QueryDslConfig.class})
class BudgetRepositoryTest {

  @Autowired private BudgetRepository budgetRepository;
  @Autowired private UserRepository userRepository;

  private User testUser;
  private User otherUser;

  @BeforeEach
  void setUp() {
    testUser = userRepository.save(User.createTestUser());
    otherUser = userRepository.save(User.createTestOtherUser());
  }

  @Nested
  @DisplayName("1. 기본 CRUD 테스트")
  class BasicCrudTests {

    @Test
    @DisplayName("1-1. 예산을 저장할 수 있다")
    void saveBudget() {
      // Given: 새 예산
      Budget budget =
          Budget.builder()
              .user(testUser)
              .name("2024년 2월 식비 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .category(Category.FOOD)
              .isActive(true)
              .build();

      // When: 저장
      Budget savedBudget = budgetRepository.save(budget);

      // Then: 저장 성공 및 ID 생성 확인
      assertThat(savedBudget.getIdx()).isNotNull();
      assertThat(savedBudget.getUser()).isEqualTo(testUser);
      assertThat(savedBudget.getAmount()).isEqualByComparingTo(new BigDecimal("500000"));
    }

    @Test
    @DisplayName("1-2. ID로 예산을 조회할 수 있다")
    void findBudgetById() {
      // Given: 저장된 예산
      Budget savedBudget =
          budgetRepository.save(
              Budget.builder()
                  .user(testUser)
                  .name("2024년 2월 전체 예산")
                  .budgetPeriod(BudgetPeriod.MONTHLY)
                  .startDate(LocalDate.of(2024, 2, 1))
                  .endDate(LocalDate.of(2024, 2, 29))
                  .amount(new BigDecimal("2000000"))
                  .build());

      // When: ID로 조회
      Optional<Budget> foundBudget = budgetRepository.findById(savedBudget.getIdx());

      // Then: 조회 성공
      assertThat(foundBudget).isPresent();
      assertThat(foundBudget.get().getName()).isEqualTo("2024년 2월 전체 예산");
    }

    @Test
    @DisplayName("1-3. Soft delete된 예산은 @SQLRestriction으로 자동 필터링된다")
    void softDeleteFiltering() {
      // Given: 예산 저장 후 soft delete
      Budget budget =
          budgetRepository.save(
              Budget.builder()
                  .user(testUser)
                  .name("삭제될 예산")
                  .budgetPeriod(BudgetPeriod.MONTHLY)
                  .startDate(LocalDate.of(2024, 2, 1))
                  .endDate(LocalDate.of(2024, 2, 29))
                  .amount(new BigDecimal("100000"))
                  .category(Category.FOOD)
                  .build());

      budget.softDelete();
      budgetRepository.save(budget);

      // When: 사용자의 모든 예산 조회
      List<Budget> userBudgets = budgetRepository.findByUserAndIsActiveTrue(testUser);

      // Then: @SQLRestriction으로 인해 soft delete된 예산은 조회되지 않음
      assertThat(userBudgets).isEmpty();
    }
  }

  @Nested
  @DisplayName("2. 사용자별 조회 테스트")
  class FindByUserTests {

    @Test
    @DisplayName("2-1. 사용자의 활성화된 예산 목록을 조회할 수 있다")
    void findActivebudgets() {
      // Given: 활성화된 예산과 비활성화된 예산
      Budget activeBudget =
          budgetRepository.save(
              Budget.builder()
                  .user(testUser)
                  .name("활성 예산")
                  .budgetPeriod(BudgetPeriod.MONTHLY)
                  .startDate(LocalDate.of(2024, 2, 1))
                  .endDate(LocalDate.of(2024, 2, 29))
                  .amount(new BigDecimal("500000"))
                  .isActive(true)
                  .build());

      Budget inactiveBudget =
          budgetRepository.save(
              Budget.builder()
                  .user(testUser)
                  .name("비활성 예산")
                  .budgetPeriod(BudgetPeriod.MONTHLY)
                  .startDate(LocalDate.of(2024, 1, 1))
                  .endDate(LocalDate.of(2024, 1, 31))
                  .amount(new BigDecimal("400000"))
                  .isActive(false)
                  .build());

      // When: 활성화된 예산만 조회
      List<Budget> budgets = budgetRepository.findByUserAndIsActiveTrue(testUser);

      // Then: 활성화된 예산만 조회됨
      assertThat(budgets).hasSize(1);
      assertThat(budgets.get(0).getName()).isEqualTo("활성 예산");
    }

    @Test
    @DisplayName("2-2. 다른 사용자의 예산은 조회되지 않는다")
    void findByUser_OtherUserNotIncluded() {
      // Given: 두 사용자의 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("테스트 유저 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .isActive(true)
              .build());

      budgetRepository.save(
          Budget.builder()
              .user(otherUser)
              .name("다른 유저 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("600000"))
              .isActive(true)
              .build());

      // When: testUser의 예산 조회
      List<Budget> budgets = budgetRepository.findByUserAndIsActiveTrue(testUser);

      // Then: testUser의 예산만 조회됨
      assertThat(budgets).hasSize(1);
      assertThat(budgets.get(0).getUser()).isEqualTo(testUser);
    }
  }

  @Nested
  @DisplayName("3. 기간별 조회 테스트")
  class FindByPeriodTests {

    @Test
    @DisplayName("3-1. 특정 날짜에 해당하는 예산을 조회할 수 있다")
    void findBudgetsForDate() {
      // Given: 2월 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("2월 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .build());

      // When: 2024-02-15가 포함된 예산 조회
      LocalDate targetDate = LocalDate.of(2024, 2, 15);
      List<Budget> budgets =
          budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              testUser, targetDate, targetDate);

      // Then: 2월 예산이 조회됨
      assertThat(budgets).hasSize(1);
      assertThat(budgets.get(0).getName()).isEqualTo("2월 예산");
    }

    @Test
    @DisplayName("3-2. 기간 밖의 날짜로는 예산이 조회되지 않는다")
    void findBudgetsForDate_OutOfRange() {
      // Given: 2월 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("2월 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .build());

      // When: 3월 날짜로 조회
      LocalDate targetDate = LocalDate.of(2024, 3, 15);
      List<Budget> budgets =
          budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              testUser, targetDate, targetDate);

      // Then: 조회되지 않음
      assertThat(budgets).isEmpty();
    }
  }

  @Nested
  @DisplayName("4. 카테고리별 조회 테스트")
  class FindByCategoryTests {

    @Test
    @DisplayName("4-1. 특정 카테고리의 예산을 조회할 수 있다")
    void findBudgetByCategory() {
      // Given: 식비 카테고리 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("2월 식비 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .category(Category.FOOD)
              .build());

      // When: 식비 카테고리 2월 예산 조회
      Optional<Budget> budget =
          budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              testUser, Category.FOOD, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

      // Then: 식비 예산이 조회됨
      assertThat(budget).isPresent();
      assertThat(budget.get().getCategory()).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("4-2. 다른 카테고리의 예산은 조회되지 않는다")
    void findBudgetByCategory_DifferentCategory() {
      // Given: 식비 카테고리 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("2월 식비 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .category(Category.FOOD)
              .build());

      // When: 교통비 카테고리로 조회
      Optional<Budget> budget =
          budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              testUser, Category.TRANSPORT, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

      // Then: 조회되지 않음
      assertThat(budget).isEmpty();
    }
  }

  @Nested
  @DisplayName("5. 알림 체크 테스트")
  class AlertCheckTests {

    @Test
    @DisplayName("5-1. 알림 체크가 필요한 예산을 조회할 수 있다")
    void findActiveBudgetsForAlertCheck() {
      // Given: 알림 미발송 예산과 알림 발송 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("알림 미발송 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .isActive(true)
              .isAlertSent(false)
              .build());

      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("알림 발송 완료 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("600000"))
              .isActive(true)
              .isAlertSent(true)
              .build());

      // When: 2024-02-15 기준으로 알림 체크 대상 조회
      List<Budget> budgets =
          budgetRepository.findActiveBudgetsForAlertCheck(LocalDate.of(2024, 2, 15));

      // Then: 알림 미발송 예산만 조회됨
      assertThat(budgets).hasSize(1);
      assertThat(budgets.get(0).getName()).isEqualTo("알림 미발송 예산");
    }

    @Test
    @DisplayName("5-2. 비활성화된 예산은 알림 체크 대상에서 제외된다")
    void findActiveBudgetsForAlertCheck_InactiveExcluded() {
      // Given: 활성 예산과 비활성 예산
      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("활성 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .isActive(true)
              .isAlertSent(false)
              .build());

      budgetRepository.save(
          Budget.builder()
              .user(testUser)
              .name("비활성 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("600000"))
              .isActive(false)
              .isAlertSent(false)
              .build());

      // When: 알림 체크 대상 조회
      List<Budget> budgets =
          budgetRepository.findActiveBudgetsForAlertCheck(LocalDate.of(2024, 2, 15));

      // Then: 활성 예산만 조회됨
      assertThat(budgets).hasSize(1);
      assertThat(budgets.get(0).getName()).isEqualTo("활성 예산");
    }
  }
}
