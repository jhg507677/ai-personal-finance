package com.codingcat.aipersonalfinance.module.security.token;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /*SELECT * FROM token WHERE refreshToken = #{refreshToken}*/
  // refreshToken은 유니크가 아닐텐데?
  // 왜냐면 어떤 refreshToken으로 accessToken 갱신했는지 알아야하니까
  Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
