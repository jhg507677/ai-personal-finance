package com.codingcat.aipersonalfinance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass // 이 클래스를 상속한 엔티티는 이 클래스의 필드를 자기 컬럼처럼 가짐
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
  @CreatedDate
  @Column(name = "created_date_time", nullable = false, updatable = false)
  private LocalDateTime createdDateTime;

  @LastModifiedDate
  @Column(name = "modified_date_time")
  private LocalDateTime modifiedDateTime;

  @Column(name = "deleted_at")
  private LocalDateTime deletedDateTime;

  /**
   * 엔티티를 논리적으로 삭제합니다.
   */
  public void softDelete() {
    this.deletedDateTime = LocalDateTime.now();
  }

  /**
   * 엔티티가 삭제되었는지 확인합니다.
   *
   * @return 삭제된 경우 true, 아니면 false
   */
  public boolean isDeleted() {
    return this.deletedDateTime != null;
  }

  /**
   * 논리적으로 삭제된 엔티티를 복구합니다.
   */
  public void restore() {
    this.deletedDateTime = null;
  }
}
