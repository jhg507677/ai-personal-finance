package com.codingcat.aipersonalfinance.module.security;

import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userIdx) throws UsernameNotFoundException {
    User user = userRepository.findById(Long.valueOf(userIdx)).orElse(null);
    if(user == null) throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
    return UserPrincipal.from(user);
  }
}
