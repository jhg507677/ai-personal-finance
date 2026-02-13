package com.codingcat.aipersonalfinance.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Ledger, Long> {

}
