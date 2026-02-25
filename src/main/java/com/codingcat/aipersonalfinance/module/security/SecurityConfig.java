package com.codingcat.aipersonalfinance.module.security;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import com.codingcat.aipersonalfinance.module.config.oauth.Oauth2SuccessHandler;
import com.codingcat.aipersonalfinance.module.config.oauth.Oauth2UserCustomService;
import com.codingcat.aipersonalfinance.module.config.oauth.Oauth2AuthorizationRequestBaseOnCookieRepository;
import com.codingcat.aipersonalfinance.module.security.filter.JwtFilter;
import com.codingcat.aipersonalfinance.module.security.handler.JwtAccessDeniedHandler;
import com.codingcat.aipersonalfinance.module.security.handler.JwtAuthenticationEntryPoint;
import com.codingcat.aipersonalfinance.module.security.token.RefreshTokenRepository;
import com.codingcat.aipersonalfinance.module.security.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {



  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final TokenProvider tokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final AdminDetailService adminDetailService;
  private final Oauth2UserCustomService oAuth2UserCustomService;
  private final Oauth2SuccessHandler oauth2SuccessHandler;

  // 스프링시큐리티의 모든 기능을 사용하지 않음
  // 즉 인증, 인가, 서비스를 모든 곳에 적용하지 않음
  @Bean
  public WebSecurityCustomizer configure(){
    return (web) -> web.ignoring()
      .requestMatchers(toH2Console())
      .requestMatchers("/swagger-ui/**")
      .requestMatchers("/v3/api-docs/**")
      .requestMatchers("/h2-console/**")
      .requestMatchers("/static/**")
      .requestMatchers("/health")
      .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
      ;
  }


  // authenticated 인증이 필요한 곳
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .csrf(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/public/**"
        ).permitAll()
        .anyRequest().authenticated()
      )
      // Oauth2에 필요한 정보를 세션이 아닌 쿠키에 저장해서 쓸수 있도록 설정
      .oauth2Login(oauth -> oauth
        .authorizationEndpoint(authorization -> authorization
          .authorizationRequestRepository(new Oauth2AuthorizationRequestBaseOnCookieRepository())
        )
        .userInfoEndpoint(userInfo -> userInfo
          .userService(oAuth2UserCustomService)
        ).successHandler(oauth2SuccessHandler)
      )
      .sessionManagement((session -> {
        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
      }))
      // *********** 에러 핸들러 ***********
      .exceptionHandling(authenticationManager -> authenticationManager
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(jwtAccessDeniedHandler))

      // JWT 토큰을 감지하는 필터를 추가
      .addFilterBefore(new JwtFilter(tokenProvider, adminDetailService,userDetailsService), UsernamePasswordAuthenticationFilter.class)
    ;
    return http.build();
  }

  // 비밀번호를 암호화하기 위한 인코더 설정(Sha256 암호화 방식 사용)
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // 암호화 연산 반복 획수, 높을수록 해킹 어려움
  }
}
