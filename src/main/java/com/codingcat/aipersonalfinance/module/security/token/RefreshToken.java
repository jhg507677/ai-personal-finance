package com.codingcat.aipersonalfinance.module.security.token;

import com.codingcat.commerce.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB 기록용
@Table(name="refresh_token")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class RefreshToken  extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "token_idx", updatable = false)
  private Long idx;

  @Column(nullable = true, unique = true) private String refreshToken;

  @Schema(description = "토큰 만료시간")
  private Instant expiredDateTime;

  @Column(name = "user_idx", nullable = true)
  private Long userIdx;

  @Column(name = "admin_idx", nullable = true)
  private Long adminIdx;

  @Schema(description = "갱신 횟수") private int refreshCount;

  public void increaseRefreshCount() {
    this.refreshCount++;
  }
}
