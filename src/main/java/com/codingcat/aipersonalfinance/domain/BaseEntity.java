package com.codingcat.aipersonalfinance.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass // 이 클래스를 상속한 엔티티는 이 클래스의 필드를 자기 컬럼처럼 가짐
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
  @CreatedDate
  private LocalDateTime createdDateTime;

  @LastModifiedDate
  private LocalDateTime modifiedDateTime;
}
