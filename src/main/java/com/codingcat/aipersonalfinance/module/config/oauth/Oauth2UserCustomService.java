package com.codingcat.aipersonalfinance.module.config.oauth;

import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class Oauth2UserCustomService extends DefaultOAuth2UserService {
  private final UserRepository userRepository;
  @Override
  public OAuth2User loadUser(
    OAuth2UserRequest userRequest
  ) throws OAuth2AuthenticationException {
    // 요청을 바타응로 유저 정보를 담은 객체 반환
    OAuth2User user = super.loadUser(userRequest);
    saveOrUpdate(user);
    return user;
  }

  // Oauth 서비스에서 제공하는 정보를 기준으로 유저가 있으면 업데이트 ,없으면 유저 생성
  private void saveOrUpdate(
    OAuth2User oAuth2User
  ) {
    Map<String, Object> attribute = oAuth2User.getAttributes();
    String email = (String) attribute.get("email");
    String name = (String) attribute.get("name");
    User user = userRepository.findByEmail(email)
      .orElseGet(() ->
        User.builder()
          .email(email)
          .nickname(name)
          .password("")
          .build()
      );

    user.changeNickname(name);
    userRepository.save(user);
  }
}
