package com.codingcat.aipersonalfinance.module.security;

import com.codingcat.commerce.domain.admin.Admin;
import com.codingcat.commerce.domain.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminDetailService implements UserDetailsService {
  private final AdminRepository adminRepository;

  @Override
  public UserDetails loadUserByUsername(String userIdx) throws UsernameNotFoundException {
    Admin admin = adminRepository.findById(Long.valueOf(userIdx)).orElse(null);
    if(admin == null) throw new UsernameNotFoundException("존재하지 않는 이메일입니다.");
    return AdminPrincipal.from(admin);
  }
}
