package com.codingcat.aipersonalfinance.module.model;

import java.util.List;
import lombok.Builder;

@Builder
public class ListResponse<T> {
  private List<T> items;
  private long totalCount;
}
