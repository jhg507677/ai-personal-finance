package com.codingcat.aipersonalfinance.domain.user;

import com.codingcat.aipersonalfinance.domain.BaseEntity;
import com.codingcat.aipersonalfinance.module.model.ServiceType;
import com.codingcat.aipersonalfinance.module.security.AuthDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name="users")
@Entity // 해당 객체를 JPA관리하는 엔티티로 지정, 즉 Customer 클래스와 실제 customer 테이블을 매핑, 이름을 다르게 하고 싶다면 name 속성 사용
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_idx", updatable = false)
  private Long idx;

  @Column(name = "id", nullable = false, unique = true, length = 50)
  private String userId;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  @Column(nullable = true)
  private String role;

  /*
  서비스코드에서 업데이트 기능을 사용할려면 서비스 메서드에 반드시 @Transactional을 붙여야함
  */
  public void changeName(String name){
    this.name = name;
  }

  public static User createTestUser() {
    User user = new User();
    user.userId = "testId";
    user.email = "test@test.com";
    user.name = "테스트유저";
    user.password = "password";
    user.role = "USER";
    return user;
  }

  public static User createTestOtherUser() {
    User user = new User();
    user.userId = "testId";
    user.email = "other@test.com";
    user.name = "다른유저";
    user.password = "password";
    user.role = "USER";
    return user;
  }

  public AuthDto toAuth(){
    return AuthDto.builder()
      .serviceType(ServiceType.USER)
      .userId(userId)
      .email(email)
      .build();
  }
}
