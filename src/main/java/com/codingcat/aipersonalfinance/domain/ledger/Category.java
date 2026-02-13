package com.codingcat.aipersonalfinance.domain.ledger;

public enum Category {
  FOOD("식비"),
  TRANSPORT("교통비"),
  CAFE("카페/간식"),
  SHOPPING("쇼핑"),
  LIVING("생활용품"),
  COMMUNICATION("통신비"),
  MEDICAL("의료비"),
  EDUCATION("교육"),
  SUBSCRIPTION("구독"),
  ETC("기타");

  private final String desc;

  Category(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}