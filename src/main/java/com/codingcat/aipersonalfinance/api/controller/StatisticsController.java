package com.codingcat.aipersonalfinance.api.controller;

import com.codingcat.aipersonalfinance.api.service.StatisticsService;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 API 컨트롤러
 */
@Tag(name = "Statistics", description = "통계 API")
@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 월별 통계 조회
     */
    @Operation(summary = "월별 통계 조회", description = "지정된 기간의 월별 수입/지출 통계를 조회합니다.")
    @GetMapping("/api/v1/client/statistics/monthly")
    public ResponseEntity<?> getMonthlyStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getMonthlyStatistics(userPrincipal.getAuthDto(), startDate, endDate);
    }

    /**
     * 카테고리별 통계 조회
     */
    @Operation(summary = "카테고리별 통계 조회", description = "지정된 기간의 카테고리별 지출 통계를 조회합니다.")
    @GetMapping("/api/v1/client/statistics/category")
    public ResponseEntity<?> getCategoryStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getCategoryStatistics(userPrincipal.getAuthDto(), startDate, endDate);
    }

    /**
     * 결제수단별 통계 조회
     */
    @Operation(summary = "결제수단별 통계 조회", description = "지정된 기간의 결제수단별 통계를 조회합니다.")
    @GetMapping("/api/v1/client/statistics/payment-method")
    public ResponseEntity<?> getPaymentMethodStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getPaymentMethodStatistics(userPrincipal.getAuthDto(), startDate, endDate);
    }

    /**
     * 지출 트렌드 분석
     */
    @Operation(summary = "지출 트렌드 분석", description = "전월 대비 당월의 수입/지출 증감율을 분석합니다.")
    @GetMapping("/api/v1/client/statistics/trend")
    public ResponseEntity<?> getTrendAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetMonth) {
        return statisticsService.getTrendAnalysis(userPrincipal.getAuthDto(), targetMonth);
    }

    /**
     * Top N 카테고리 조회
     */
    @Operation(summary = "Top N 카테고리 조회", description = "지출이 많은 상위 N개 카테고리를 조회합니다.")
    @GetMapping("/api/v1/client/statistics/top-categories")
    public ResponseEntity<?> getTopCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {
        return statisticsService.getTopCategories(userPrincipal.getAuthDto(), startDate, endDate, limit);
    }
}
