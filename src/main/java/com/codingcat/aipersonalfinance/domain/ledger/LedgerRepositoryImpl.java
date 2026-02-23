package com.codingcat.aipersonalfinance.domain.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.dto.CategorySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.MonthlySummary;
import com.codingcat.aipersonalfinance.domain.ledger.dto.PaymentMethodSummary;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 통계 조회를 위한 커스텀 Repository 구현
 * QueryDSL을 사용한 복잡한 집계 쿼리
 */
@RequiredArgsConstructor
public class LedgerRepositoryImpl implements LedgerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
