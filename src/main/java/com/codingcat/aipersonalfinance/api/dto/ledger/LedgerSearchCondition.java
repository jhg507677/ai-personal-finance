package com.codingcat.aipersonalfinance.api.dto.ledger;

import com.codingcat.aipersonalfinance.domain.ledger.Category;
import com.codingcat.aipersonalfinance.domain.ledger.LedgerType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 내역 검색 조건 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerSearchCondition {

  private LedgerType type;
  private Category category;
  private LocalDate startDate;
  private LocalDate endDate;
}
