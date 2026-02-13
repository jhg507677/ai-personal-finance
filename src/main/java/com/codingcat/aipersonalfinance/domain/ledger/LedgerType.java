package com.codingcat.aipersonalfinance.domain.ledger;

public enum LedgerType {
  INCOME("수입"),
  EXPENSE("지출")
  ;

  private String desc;

  LedgerType(String desc) {
    this.desc = desc;
  }
}
