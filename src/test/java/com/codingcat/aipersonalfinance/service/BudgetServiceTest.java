package com.codingcat.aipersonalfinance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codingcat.aipersonalfinance.domain.budget.BudgetService;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetCreateRequest;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetResponse;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetUpdateRequest;
import com.codingcat.aipersonalfinance.domain.budget.dto.BudgetUsageResponse;
import com.codingcat.aipersonalfinance.domain.budget.Budget;
import com.codingcat.aipersonalfinance.domain.budget.BudgetPeriod;
import com.codingcat.aipersonalfinance.domain.budget.BudgetRepository;
import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerRepository;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * BudgetService 테스트
 */
@DisplayName("BudgetService 테스트")
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

  @Mock private BudgetRepository budgetRepository;
  @Mock private UserRepository userRepository;
  @Mock private LedgerRepository ledgerRepository;

  @InjectMocks private BudgetService budgetService;

  private User testUser;
  private Budget testBudget;

  @BeforeEach
  void setUp() {
    testUser = User.createTestUser();

    testBudget =
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
  }

  @Nested
  @DisplayName("1. 예산 생성 테스트")
  class CreateBudgetTests {

    @Test
    @DisplayName("1-1. 유효한 요청으로 예산을 생성할 수 있다")
    void createBudget_Success() {
      // Given
      String userId = "testId";
      BudgetCreateRequest request =
          BudgetCreateRequest.builder()
              .name("2024년 2월 식비 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .category(Category.FOOD)
              .build();

      given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));
      given(budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              any(), any(), any(), any()))
          .willReturn(Optional.empty()); // 기간 겹침 없음
      given(budgetRepository.save(any(Budget.class))).willReturn(testBudget);

      // When
      BudgetResponse response = budgetService.createBudget(userId, request);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getName()).isEqualTo("2024년 2월 식비 예산");
      assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500000"));
      verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("1-2. 같은 카테고리, 같은 기간에 중복된 예산 생성 시 예외가 발생한다")
    void createBudget_DuplicatePeriod() {
      // Given
      String userId = "testId";
      BudgetCreateRequest request =
          BudgetCreateRequest.builder()
              .name("2024년 2월 식비 예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .category(Category.FOOD)
              .build();

      given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));
      given(budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
              any(), any(), any(), any()))
          .willReturn(Optional.of(testBudget)); // 기간 겹침 발견

      // When & Then
      assertThatThrownBy(() -> budgetService.createBudget(userId, request))
          .isInstanceOf(BusinessException.class);

      verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("1-3. 존재하지 않는 사용자로 예산 생성 시 예외가 발생한다")
    void createBudget_UserNotFound() {
      // Given
      String userId = "invalidId";
      BudgetCreateRequest request =
          BudgetCreateRequest.builder()
              .name("예산")
              .budgetPeriod(BudgetPeriod.MONTHLY)
              .startDate(LocalDate.of(2024, 2, 1))
              .endDate(LocalDate.of(2024, 2, 29))
              .amount(new BigDecimal("500000"))
              .build();

      given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> budgetService.createBudget(userId, request))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("2. 예산 조회 테스트")
  class GetBudgetTests {

    @Test
    @DisplayName("2-1. ID로 예산 상세 정보를 조회할 수 있다")
    void getBudget_Success() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When
      BudgetResponse response = budgetService.getBudget(userId, budgetId);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getName()).isEqualTo("2024년 2월 식비 예산");
    }

    @Test
    @DisplayName("2-2. 존재하지 않는 예산 조회 시 예외가 발생한다")
    void getBudget_NotFound() {
      // Given
      Long budgetId = 999L;
      String userId = "testId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> budgetService.getBudget(userId, budgetId))
          .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("2-3. 다른 사용자의 예산 조회 시 예외가 발생한다")
    void getBudget_AccessDenied() {
      // Given
      Long budgetId = 1L;
      String otherUserId = "otherTestId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When & Then
      assertThatThrownBy(() -> budgetService.getBudget(otherUserId, budgetId))
          .isInstanceOf(BusinessException.class);
    }
  }

  @Nested
  @DisplayName("3. 예산 사용 현황 계산 테스트")
  class GetBudgetUsageTests {

    @Test
    @DisplayName("3-1. 예산 사용 현황을 정확히 계산할 수 있다")
    void getBudgetUsage_Success() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));
      // 300,000원 지출 (예산의 60%)
      given(ledgerRepository.calculateTotalByTypeAndDateRange(any(), any(), any(), any()))
          .willReturn(new BigDecimal("300000"));

      // When
      BudgetUsageResponse response = budgetService.getBudgetUsage(userId, budgetId);

      // Then
      assertThat(response).isNotNull();
      assertThat(response.getTotalSpent()).isEqualByComparingTo(new BigDecimal("300000"));
      assertThat(response.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("200000"));
      assertThat(response.getUsagePercentage()).isEqualByComparingTo(new BigDecimal("60.00"));
      assertThat(response.getIsExceeded()).isFalse();
    }

    @Test
    @DisplayName("3-2. 예산 초과 시 isExceeded가 true를 반환한다")
    void getBudgetUsage_Exceeded() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));
      // 600,000원 지출 (예산의 120%)
      given(ledgerRepository.calculateTotalByTypeAndDateRange(any(), any(), any(), any()))
          .willReturn(new BigDecimal("600000"));

      // When
      BudgetUsageResponse response = budgetService.getBudgetUsage(userId, budgetId);

      // Then
      assertThat(response.getTotalSpent()).isEqualByComparingTo(new BigDecimal("600000"));
      assertThat(response.getRemainingAmount())
          .isEqualByComparingTo(new BigDecimal("-100000")); // 음수
      assertThat(response.getUsagePercentage()).isEqualByComparingTo(new BigDecimal("120.00"));
      assertThat(response.getIsExceeded()).isTrue();
    }

    @Test
    @DisplayName("3-3. 알림 임계값 초과 시 shouldAlert가 true를 반환한다")
    void getBudgetUsage_ShouldAlert() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";
      // 알림 임계값 80%

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));
      // 450,000원 지출 (예산의 90%)
      given(ledgerRepository.calculateTotalByTypeAndDateRange(any(), any(), any(), any()))
          .willReturn(new BigDecimal("450000"));

      // When
      BudgetUsageResponse response = budgetService.getBudgetUsage(userId, budgetId);

      // Then
      assertThat(response.getUsagePercentage()).isEqualByComparingTo(new BigDecimal("90.00"));
      assertThat(response.getShouldAlert()).isTrue(); // 90% > 80%
    }
  }

  @Nested
  @DisplayName("4. 예산 수정 테스트")
  class UpdateBudgetTests {

    @Test
    @DisplayName("4-1. 본인의 예산을 수정할 수 있다")
    void updateBudget_Success() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";
      BudgetUpdateRequest request =
          BudgetUpdateRequest.builder().amount(new BigDecimal("600000")).build();

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When
      BudgetResponse response = budgetService.updateBudget(userId, budgetId, request);

      // Then
      assertThat(response).isNotNull();
      verify(budgetRepository).findById(budgetId);
    }

    @Test
    @DisplayName("4-2. 다른 사용자의 예산 수정 시 예외가 발생한다")
    void updateBudget_AccessDenied() {
      // Given
      Long budgetId = 1L;
      String otherUserId = "otherTestId";
      BudgetUpdateRequest request =
          BudgetUpdateRequest.builder().amount(new BigDecimal("600000")).build();

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When & Then
      assertThatThrownBy(() -> budgetService.updateBudget(otherUserId, budgetId, request))
          .isInstanceOf(BusinessException.class);
    }
  }

  @Nested
  @DisplayName("5. 예산 삭제 테스트")
  class DeleteBudgetTests {

    @Test
    @DisplayName("5-1. 본인의 예산을 삭제할 수 있다 (Soft Delete)")
    void deleteBudget_Success() {
      // Given
      Long budgetId = 1L;
      String userId = "testId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When
      budgetService.deleteBudget(userId, budgetId);

      // Then
      verify(budgetRepository).findById(budgetId);
      assertThat(testBudget.getDeletedDateTime()).isNotNull();
    }

    @Test
    @DisplayName("5-2. 다른 사용자의 예산 삭제 시 예외가 발생한다")
    void deleteBudget_AccessDenied() {
      // Given
      Long budgetId = 1L;
      String otherUserId = "otherTestId";

      given(budgetRepository.findById(budgetId)).willReturn(Optional.of(testBudget));

      // When & Then
      assertThatThrownBy(() -> budgetService.deleteBudget(otherUserId, budgetId))
          .isInstanceOf(BusinessException.class);
    }
  }

  @Nested
  @DisplayName("6. 예산 목록 조회 테스트")
  class GetBudgetListTests {

    @Test
    @DisplayName("6-1. 사용자의 활성화된 예산 목록을 조회할 수 있다")
    void getBudgetList_Success() {
      // Given
      String userId = "testId";

      given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));
      given(budgetRepository.findByUserAndIsActiveTrue(testUser)).willReturn(List.of(testBudget));

      // When
      List<BudgetResponse> response = budgetService.getBudgetList(userId);

      // Then
      assertThat(response).isNotNull();
      assertThat(response).hasSize(1);
      assertThat(response.get(0).getName()).isEqualTo("2024년 2월 식비 예산");
    }
  }
}
