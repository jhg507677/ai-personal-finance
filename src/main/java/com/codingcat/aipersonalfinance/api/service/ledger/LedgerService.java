package com.codingcat.aipersonalfinance.api.service.ledger;


import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

  public ResponseEntity<ApiResponseVo<?>> createLedger(
    AddLedgerRequest request
  ) {
    return null;
  }
}
