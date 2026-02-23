package com.codingcat.aipersonalfinance.domain.user;

import com.codingcat.aipersonalfinance.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
  @NotBlank(message = "id값은 필수입니다.")
  private String userId;

  @NotBlank(message = "성명은 필수입니다.")
  private String name;

  @NotBlank(message = "이메일은 필수입니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  private String password;

  public User toEntity(){
    return User.builder()
      .userId(userId)
      .name(name)
      .email(email)
      .password(password)
      .build();
  }
}
