package com.codingcat.aipersonalfinance.domain.ledger;

public enum PaymentMethod {
  CARD("카드"),
  CASH("현금"),
  TRANSFER("계좌이체"),
  KAKAOPAY("카카오페이"),
  NAVERPAY("네이버페이")
  ;

  private String desc;

  PaymentMethod(String desc) {
    this.desc = desc;
  }
}
