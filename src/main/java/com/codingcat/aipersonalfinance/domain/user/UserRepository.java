package com.codingcat.aipersonalfinance.domain.user;

import java.util.Optional;
import javax.xml.validation.Schema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByName(String name);
  Optional<User> findByUserId(String userId);
  Optional<User>  findByEmail(String email);
}