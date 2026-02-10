package com.codingcat.aipersonalfinance.domain.memo;

import com.codingcat.aipersonalfinance.domain.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {

}
