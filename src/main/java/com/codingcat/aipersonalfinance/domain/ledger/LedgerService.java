package com.codingcat.aipersonalfinance.domain.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerCreateRequest;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerResponse;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerSearchRequest;
import com.codingcat.aipersonalfinance.domain.ledger.dto.LedgerUpdateRequest;
import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.exception.CustomException;
import com.codingcat.aipersonalfinance.module.response.PageResponse;
import com.codingcat.aipersonalfinance.module.security.AuthDto;

import static com.codingcat.aipersonalfinance.module.response.ApiResponseUtil.sendApiOK;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

  // 거래 내역 생성
  @Transactional
  public ResponseEntity<?> createLedger(AuthDto authDto, LedgerCreateRequest request) {
    User user = findUserByEmail(authDto.getEmail());
    Ledger ledger = request.toEntity(user);
    Ledger savedLedger = ledgerRepository.save(ledger);
    return sendApiOK(LedgerResponse.from(savedLedger));
  }

  // 거래 내역 조회
  public ResponseEntity<?> getLedger(AuthDto authDto, Long ledgerId) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getEmail(), ledger);
    return sendApiOK(LedgerResponse.from(ledger));
  }

  // 거래 내역 수정
  @Transactional
  public ResponseEntity<?> updateLedger(
      AuthDto authDto, Long ledgerId, LedgerUpdateRequest request) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getEmail(), ledger);

    ledger.update(
        request.getType(),
        request.getAmount(),
        request.getDesc(),
        request.getPlace(),
        request.getCategory(),
        request.getPaymentMethod(),
        request.getRecordedDate());

    return sendApiOK(LedgerResponse.from(ledger));
  }

  @Transactional
  public ResponseEntity<?> deleteLedger(AuthDto authDto, Long ledgerId) {
    Ledger ledger = findLedgerById(ledgerId);
    validateLedgerOwnership(authDto.getEmail(), ledger);
    ledger.sDelete();
    return sendApiOK(null);
  }

  // 거래 내역 목록 조회
  public ResponseEntity<?> getLedgerList(
      AuthDto authDto, LedgerSearchRequest condition, Pageable pageable) {
    User user = findUserByEmail(authDto.getEmail());
    Page<Ledger> ledgers = ledgerRepository.findByPageInLedger(user, condition, pageable);
    Page<LedgerResponse> responses = ledgers.map(LedgerResponse::from);
    return sendApiOK(PageResponse.from(responses));
  }

  /**
   * 이메일로 사용자를 찾습니다.
   */
  private User findUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
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
  private void validateLedgerOwnership(String email, Ledger ledger) {
    if (!ledger.getUser().getEmail().equals(email)) {
      throw new CustomException(
          HttpStatus.FORBIDDEN,
          "sm.ledger.fail.access_denied",
          "본인의 거래 내역만 접근할 수 있습니다.");
    }
  }
}
