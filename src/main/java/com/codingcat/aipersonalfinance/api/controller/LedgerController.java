package com.codingcat.aipersonalfinance.api.controller;

import com.codingcat.aipersonalfinance.api.service.ledger.AddLedgerRequest;
import com.codingcat.aipersonalfinance.api.service.ledger.LedgerService;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.module.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LedgerController {
  private final LedgerService ledgerService;

  @Operation(summary = "가계부 등록", description = "")
  @PostMapping("/api/v1/client/ledger")
  public ResponseEntity<ApiResponseVo<?>> signIn(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @Valid @RequestBody AddLedgerRequest request

  ){
    return ledgerService.createLedger(userPrincipal.getAuth(), request);
  }
}
