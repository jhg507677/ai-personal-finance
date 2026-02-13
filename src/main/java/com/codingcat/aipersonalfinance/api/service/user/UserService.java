package com.codingcat.aipersonalfinance.api.service.user;

import com.codingcat.aipersonalfinance.domain.user.User;
import com.codingcat.aipersonalfinance.domain.user.UserRepository;
import com.codingcat.aipersonalfinance.module.exception.CustomException;
import com.codingcat.aipersonalfinance.module.model.ApiResponseUtil;
import com.codingcat.aipersonalfinance.module.model.ApiResponseVo;
import com.codingcat.aipersonalfinance.module.security.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.validation.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthService authService;



  /* 회원가입 */
  public ResponseEntity<ApiResponseVo<?>> signUp(AddUserRequest request){
    // 비밀번호 암호화
    request.setPassword(passwordEncoder.encode(request.getPassword()));
    Long savedIdx = userRepository.save(request.toEntity()).getIdx();
    return ApiResponseUtil.sendApiResponse(HttpStatus.OK, "sm.common.success.default", "success", savedIdx, null);
  }

  /*로그인*/
  public ResponseEntity<ApiResponseVo<?>> login(
    HttpServletResponse response,
    AddUserRequest request
  ) {
    // 아이디 검증
    User user = authService.findByUserId(request.getUserId());

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException(HttpStatus.BAD_REQUEST, "sm.common.fail.invalid_invalid_request","로그인 할 수없는 계정입니다.");
    }
    return authService.generateLoginToken(response, user.toAuth());
  }



  /*유저 상세 조회*/
  public ResponseEntity<ApiResponseVo<?>> getUser(Long idx) {
    User user = userRepository.findById(idx).orElse(null);
    if(user == null){
      throw new CustomException(HttpStatus.NOT_FOUND, "sm.common.fail.invalid_invalid_request","존재하지 않는 유저입니다.");
    }
    return ApiResponseUtil.sendApiResponse(HttpStatus.OK, "sm.common.success.default", "success", user, null);
  }

  public ResponseEntity<ApiResponseVo<?>> refresh(
    String refreshToken
  ) {
    String accessToken = authService.createNewAccessToken(refreshToken);
    return ApiResponseUtil.sendApiResponse(HttpStatus.OK, "sm.common.success.default", "success", accessToken, null);
  }
}
