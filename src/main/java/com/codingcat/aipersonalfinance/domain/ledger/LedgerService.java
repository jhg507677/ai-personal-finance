package com.codingcat.aipersonalfinance.domain.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerCreateRequest;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerResponse;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerSearchCondition;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerUpdateRequest;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.exception.CustomException;
import com.codingcat.aipersonalfinance.module.security.AuthDto;

import static com.codingcat.aipersonalfinance.module.response.ApiResponseUtil.sendApiOK;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ledger(거래 내역) 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

  private final LedgerRepository ledgerRepository;
  private final UserRepository userRepository;

  @Transactional
  public ResponseEntity<?> createLedger(AuthDto authDto, LedgerCreateRequest request) {
    User user = findUserByUserId(authDto.getUserId());
    Ledger ledger = request.toEntity(user);
    Ledger savedLedger = ledgerRepository.save(ledger);
    return sendApiOK(
        LedgerResponse.from(savedLedger));
  }

  public ResponseEntity<?> getLedger(AuthDto authDto, Long ledgerId) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getUserId(), ledger);
    return sendApiOK(
        LedgerResponse.from(ledger));
  }

  @Transactional
  public ResponseEntity<?> updateLedger(
      AuthDto authDto, Long ledgerId, LedgerUpdateRequest request) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getUserId(), ledger);

    ledger.update(
        request.getType(),
        request.getAmount(),
        request.getDesc(),
        request.getPlace(),
        request.getCategory(),
        request.getPaymentMethod(),
        request.getRecordedDate());

    return sendApiOK(
        LedgerResponse.from(ledger));
  }

  @Transactional
  public ResponseEntity<?> deleteLedger(AuthDto authDto, Long ledgerId) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getUserId(), ledger);
    ledger.softDelete();
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK, "sm.common.success.default", "success", null, null);
  }

  public ResponseEntity<?> getLedgerList(
      AuthDto authDto, LedgerSearchCondition condition, Pageable pageable) {
    User user = findUserByUserId(authDto.getUserId());

    List<Ledger> ledgers = findLedgersByCondition(user, condition);

    List<LedgerResponse> responses =
        ledgers.stream().map(LedgerResponse::from).collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), responses.size());
    List<LedgerResponse> pagedResponses = responses.subList(start, end);

    Page<LedgerResponse> page = new PageImpl<>(pagedResponses, pageable, responses.size());

    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK, "sm.common.success.default", "success", page, null);
  }

  /**
   * 검색 조건에 따라 거래 내역을 조회합니다.
   */
  private List<Ledger> findLedgersByCondition(User user, LedgerSearchCondition condition) {
    if (condition == null) {
      return ledgerRepository.findByUserOrderByRecordedDateDesc(user);
    }

    // 조건이 있는 경우 필터링
    if (condition.getType() != null && condition.getCategory() == null) {
      return ledgerRepository.findByUserAndType(user, condition.getType());
    }

    if (condition.getCategory() != null && condition.getType() == null) {
      return ledgerRepository.findByUserAndCategory(user, condition.getCategory());
    }

    if (condition.getStartDate() != null && condition.getEndDate() != null) {
      if (condition.getCategory() != null) {
        return ledgerRepository.findByUserAndCategoryAndRecordedDateBetween(
            user, condition.getCategory(), condition.getStartDate(), condition.getEndDate());
      }
      return ledgerRepository.findByUserAndRecordedDateBetween(
          user, condition.getStartDate(), condition.getEndDate());
    }

    return ledgerRepository.findByUserOrderByRecordedDateDesc(user);
  }

  /**
   * 사용자 ID로 사용자를 찾습니다.
   */
  private User findUserByUserId(String userId) {
    return userRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "sm.common.fail.user_not_found",
                    "올바르지 않은 사용자 정보입니다."));
  }

  /**
   * 거래 내역 ID로 거래를 찾습니다.
   */
  private Ledger findLedgerById(Long ledgerId) {
    return ledgerRepository
        .findById(ledgerId)
        .orElseThrow(
            () ->
                new CustomException(
                    HttpStatus.NOT_FOUND,
                    "sm.ledger.fail.not_found",
                    "거래 내역을 찾을 수 없습니다."));
  }

  /**
   * 거래 내역의 소유자를 검증합니다.
   */
  private void validateLedgerOwnership(String userId, Ledger ledger) {
    if (!ledger.getUser().getUserId().equals(userId)) {
      throw new CustomException(
          HttpStatus.FORBIDDEN,
          "sm.ledger.fail.access_denied",
          "본인의 거래 내역만 접근할 수 있습니다.");
    }
  }
}
