package com.codingcat.aipersonalfinance.module.response;

import java.util.List;
import lombok.Builder;

@Builder
public class ListResponse<T> {
  private List<T> items;
  private long totalCount;
}
