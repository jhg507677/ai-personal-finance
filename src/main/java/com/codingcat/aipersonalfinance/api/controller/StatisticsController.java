package com.codingcat.aipersonalfinance.api.controller;

import com.codingcat.aipersonalfinance.api.dto.statistics.CategoryStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.MonthlyStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.PaymentMethodStatsResponse;
import com.codingcat.aipersonalfinance.api.dto.statistics.TrendResponse;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 API 컨트롤러
 */
@Tag(name = "Statistics", description = "통계 API")
@RestController
@RequestMapping("/api/v1/client/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 월별 통계 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 월별 통계 목록
     */
    @Operation(summary = "월별 통계 조회", description = "지정된 기간의 월별 수입/지출 통계를 조회합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponseVo<?>> getMonthlyStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MonthlyStatsResponse> result = statisticsService.getMonthlyStatistics(
                userDetails.getUsername(), startDate, endDate);

        return ResponseEntity.ok(ApiResponseVo.ok(result));
    }

    /**
     * 카테고리별 통계 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 카테고리별 통계 목록
     */
    @Operation(summary = "카테고리별 통계 조회", description = "지정된 기간의 카테고리별 지출 통계를 조회합니다.")
    @GetMapping("/category")
    public ResponseEntity<ApiResponseVo<?>> getCategoryStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CategoryStatsResponse> result = statisticsService.getCategoryStatistics(
                userDetails.getUsername(), startDate, endDate);

        return ResponseEntity.ok(ApiResponseVo.ok(result));
    }

    /**
     * 결제수단별 통계 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 결제수단별 통계 목록
     */
    @Operation(summary = "결제수단별 통계 조회", description = "지정된 기간의 결제수단별 통계를 조회합니다.")
    @GetMapping("/payment-method")
    public ResponseEntity<ApiResponseVo<?>> getPaymentMethodStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PaymentMethodStatsResponse> result = statisticsService.getPaymentMethodStatistics(
                userDetails.getUsername(), startDate, endDate);

        return ResponseEntity.ok(ApiResponseVo.ok(result));
    }

    /**
     * 지출 트렌드 분석
     *
     * @param userDetails 인증된 사용자 정보
     * @param targetMonth 분석 대상 월 (YYYY-MM-DD 형식, 해당 월의 아무 날짜)
     * @return 트렌드 분석 결과
     */
    @Operation(summary = "지출 트렌드 분석", description = "전월 대비 당월의 수입/지출 증감율을 분석합니다.")
    @GetMapping("/trend")
    public ResponseEntity<ApiResponseVo<?>> getTrendAnalysis(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetMonth) {

        TrendResponse result = statisticsService.getTrendAnalysis(
                userDetails.getUsername(), targetMonth);

        return ResponseEntity.ok(ApiResponseVo.ok(result));
    }

    /**
     * Top N 카테고리 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param startDate 시작일
     * @param endDate 종료일
     * @param limit 조회 개수 (기본값: 5)
     * @return Top N 카테고리 목록
     */
    @Operation(summary = "Top N 카테고리 조회", description = "지출이 많은 상위 N개 카테고리를 조회합니다.")
    @GetMapping("/top-categories")
    public ResponseEntity<ApiResponseVo<?>> getTopCategories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit) {

        List<CategoryStatsResponse> result = statisticsService.getTopCategories(
                userDetails.getUsername(), startDate, endDate, limit);

        return ResponseEntity.ok(ApiResponseVo.ok(result));
    }
}
