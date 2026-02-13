package com.codingcat.aipersonalfinance.api.service.ledger;


import com.codingcat.aipersonalfinance.domain.ledger.Ledger;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerRepository;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.module.security.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerService {
  private final LedgerRepository ledgerRepository;

  public ResponseEntity<ApiResponseVo<?>> createLedger(
    AuthDto auth,
    AddLedgerRequest request
  ) {
    Ledger ledger = request.toEntity();
    ledgerRepository.save(ledger);
    return null;
  }
}
