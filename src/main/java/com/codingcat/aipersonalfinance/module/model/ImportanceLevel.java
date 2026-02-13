package com.codingcat.aipersonalfinance.module.model;

import lombok.Getter;

@Getter
public enum ImportanceLevel {

  // 단계, 에러, Slack, DB, 로그, 설명
  LOG_ONLY(0, false, false, false, true, "단순 상태 로그만 기록"),
  SLACK_ONLY(1, false, true,  false, true, "에러 아님, Slack으로만 알림"),
  SLACK_AND_DB(2, false, true,  true,  true, "에러 아님, Slack + DB 모두 기록"),
  ERROR_LOG_ONLY(3, true,  false, false, true, "에러지만 Slack/DB는 보내지 않고 로그만 기록"),
  ERROR_SLACK(4, true,  true,  false, true, "에러, Slack만 기록, DB는 X"),
  ERROR_SLACK_AND_DB(5, true,  true,  true,  true, "에러, Slack + DB + 로그 모두 기록");

  private final int level;
  private final boolean isError;
  private final boolean slackNotify;
  private final boolean dbLog;
  private final boolean logOnly;
  private final String description;

  ImportanceLevel(int level, boolean isError, boolean slackNotify, boolean dbLog, boolean logOnly, String description) {
    this.level = level;
    this.isError = isError;
    this.slackNotify = slackNotify;
    this.dbLog = dbLog;
    this.logOnly = logOnly;
    this.description = description;
  }
}