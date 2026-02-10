package com.codingcat.aipersonalfinance.domain.activity;

public enum ActivityType {
  INCOME("수입"),
  EXPENSE("지출")
  ;

  private String desc;

  ActivityType(String desc) {
    this.desc = desc;
  }
}
