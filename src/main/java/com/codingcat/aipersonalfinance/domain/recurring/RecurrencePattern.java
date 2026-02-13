package com.codingcat.aipersonalfinance.domain.recurring;

/**
 * 정기 거래의 반복 주기를 나타내는 Enum
 */
public enum RecurrencePattern {
  DAILY("매일"),
  WEEKLY("매주"),
  MONTHLY("매월"),
  YEARLY("매년");

  private final String desc;

  RecurrencePattern(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
