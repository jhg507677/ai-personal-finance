package com.codingcat.aipersonalfinance.domain.ledger;

import static com.codingcat.aipersonalfinance.module.response.PageResponse.getOrderSpecifiers;
import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerSearchRequest;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 통계 조회를 위한 커스텀 Repository 구현
 * QueryDSL을 사용한 복잡한 집계 쿼리
 */
@RequiredArgsConstructor
public class LedgerRepositoryImpl implements LedgerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 조건에 맞는 거래 내역 가져오기
    @Override
    public Page<Ledger> findByPageInLedger(
      User user,
      LedgerSearchRequest condition,
      Pageable pageable
    ) {
      QLedger qLedger = QLedger.ledger;

      BooleanBuilder builder = new BooleanBuilder();
      builder.and(qLedger.user.eq(user));

      if (condition.getType() != null) builder.and(qLedger.type.eq(condition.getType()));
      if (condition.getCategory() != null) builder.and(qLedger.category.eq(condition.getCategory()));
      if (condition.getStartDate() != null && condition.getEndDate() != null) {
        builder.and(
          qLedger.recordedDate.between(
            condition.getStartDate(),
            condition.getEndDate()
          )
        );
      }

      // 🔥 Sort 변환
      List<OrderSpecifier<?>> orders =
        getOrderSpecifiers(
          pageable,
          Ledger.class,
          "ledger"
        );

      List<Ledger> content = queryFactory
        .selectFrom(qLedger)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(orders.toArray(new OrderSpecifier[0]))
        .fetch(); // 리스트

      Long total = queryFactory
        .select(qLedger.count())
        .from(qLedger)
        .where(builder)
        .fetchOne(); // 하나의 결과를 가져오고 싶을때 fetchFirst는 limit을 붙여서 에러 X

      return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<MonthlySummary> getMonthlySummary(User user, LocalDate startDate, LocalDate endDate) {
        QLedger ledger = QLedger.ledger;

        // 월별로 수입과 지출을 각각 합산
        return queryFactory
                .select(Projections.constructor(MonthlySummary.class,
                        ledger.recordedDate.year(),
                        ledger.recordedDate.month(),
                        // 수입 합계 (INCOME인 경우만)
                        Expressions.cases()
                                .when(ledger.type.eq(LedgerType.INCOME)).then(ledger.amount)
                                .otherwise(BigDecimal.ZERO)
                                .sum().coalesce(BigDecimal.ZERO),
                        // 지출 합계 (EXPENSE인 경우만)
                        Expressions.cases()
                                .when(ledger.type.eq(LedgerType.EXPENSE)).then(ledger.amount)
                                .otherwise(BigDecimal.ZERO)
                                .sum().coalesce(BigDecimal.ZERO),
                        // 순액 (수입 - 지출)
                        Expressions.cases()
                                .when(ledger.type.eq(LedgerType.INCOME)).then(ledger.amount)
                                .otherwise(BigDecimal.ZERO)
                                .sum().coalesce(BigDecimal.ZERO)
                                .subtract(
                                        Expressions.cases()
                                                .when(ledger.type.eq(LedgerType.EXPENSE)).then(ledger.amount)
                                                .otherwise(BigDecimal.ZERO)
                                                .sum().coalesce(BigDecimal.ZERO)
                                )
                ))
                .from(ledger)
                .where(ledger.user.eq(user)
                        .and(ledger.recordedDate.between(startDate, endDate)))
                .groupBy(ledger.recordedDate.year(), ledger.recordedDate.month())
                .orderBy(ledger.recordedDate.year().asc(), ledger.recordedDate.month().asc())
                .fetch();
    }

    @Override
    public List<CategorySummary> getCategorySummary(User user, LocalDate startDate, LocalDate endDate) {
        QLedger ledger = QLedger.ledger;

        return queryFactory
                .select(Projections.constructor(CategorySummary.class,
                        ledger.category,
                        ledger.amount.sum(),
                        ledger.count()
                ))
                .from(ledger)
                .where(ledger.user.eq(user)
                        .and(ledger.type.eq(LedgerType.EXPENSE))
                        .and(ledger.recordedDate.between(startDate, endDate)))
                .groupBy(ledger.category)
                .orderBy(ledger.amount.sum().desc())
                .fetch();
    }

    @Override
    public List<PaymentMethodSummary> getPaymentMethodSummary(User user, LocalDate startDate, LocalDate endDate) {
        QLedger ledger = QLedger.ledger;

        return queryFactory
                .select(Projections.constructor(PaymentMethodSummary.class,
                        ledger.paymentMethod,
                        ledger.amount.sum(),
                        ledger.count()
                ))
                .from(ledger)
                .where(ledger.user.eq(user)
                        .and(ledger.recordedDate.between(startDate, endDate)))
                .groupBy(ledger.paymentMethod)
                .orderBy(ledger.amount.sum().desc())
                .fetch();
    }

    @Override
    public List<CategorySummary> getTopCategories(User user, LocalDate startDate, LocalDate endDate, int limit) {
        QLedger ledger = QLedger.ledger;

        return queryFactory
                .select(Projections.constructor(CategorySummary.class,
                        ledger.category,
                        ledger.amount.sum(),
                        ledger.count()
                ))
                .from(ledger)
                .where(ledger.user.eq(user)
                        .and(ledger.type.eq(LedgerType.EXPENSE))
                        .and(ledger.recordedDate.between(startDate, endDate)))
                .groupBy(ledger.category)
                .orderBy(ledger.amount.sum().desc())
                .limit(limit)
                .fetch();
    }
}
