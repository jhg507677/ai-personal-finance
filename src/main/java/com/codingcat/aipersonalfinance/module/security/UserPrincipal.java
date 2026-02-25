package com.codingcat.aipersonalfinance.module.security;

import com.codingcat.aipersonalfinance.domain.user.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {
  private final String id;
  private final String password;
  private final String auth;
  private final AuthDto authDto;

  public UserPrincipal(String id, String password, String auth) {
    this.id = id;
    this.password = password;
    this.auth = auth;
    this.authDto = null; // TODO: Phase 2 이후 User 엔티티로부터 생성하도록 수정
  }

  // 사용자가 가지고 있는 권한을 반환
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("user"));
  }

  @Override
  public String getUsername() {
    return this.getId();
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  // 엔티티를 받아서 Principal 생성
  public static UserPrincipal from(User user) {
    return new UserPrincipal(user.getEmail(), user.getPassword(), user.getRole());
  }
}
