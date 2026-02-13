package com.codingcat.aipersonalfinance.module.security;

import com.codingcat.aipersonalfinance.domain.admin.Admin;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AdminPrincipal implements UserDetails {
  private final String id;
  private final String password;
  private final String auth;

  public AdminPrincipal(String id, String password, String auth) {
    this.id = id;
    this.password = password;
    this.auth = auth;
  }

  // 사용자가 가지고 있는 권한을 반환
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("admin"));
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
  public static AdminPrincipal from(Admin admin) {
    return new AdminPrincipal(admin.getEmail(), admin.getPassword(), admin.getRole());
  }
}