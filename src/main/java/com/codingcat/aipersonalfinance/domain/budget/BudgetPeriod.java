package com.codingcat.aipersonalfinance.domain.budget;

/**
 * 예산 주기를 나타내는 Enum
 */
public enum BudgetPeriod {
  WEEKLY("주간", 7),
  MONTHLY("월간", 30),
  YEARLY("연간", 365);

  private final String desc;
  private final int days;

  BudgetPeriod(String desc, int days) {
    this.desc = desc;
    this.days = days;
  }

  public String getDesc() {
    return desc;
  }

  public int getDays() {
    return days;
  }
}
