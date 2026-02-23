# Spring Boot Backend Development Rules

Project: [AI 가계부]

## 1. Overview
Spring Boot 기반 API 서버

## 2. Tech Stack
-   Runtime: Java 17
-   Framework: Spring Boot
-   ORM: JPA
-   Database: H2 (test) / MySQL (prod)
-   Auth: JWT

## 3. Project Structure
    src/
    ├── controller      # 모든 Controller (API Entry Point)
    ├── domain/         # 도메인 영역 (Service, Entity, Repository)
    ├── domain/{domain}/dto      # response, request ex)domain/ledger/dto/AddLedgerRequest
    ├── module/         # 공통 모듈 (config, exception, response, aop, util 등)

### 원칙
- Controller는 요청/응답 처리만 담당
- 권한 체크는 AOP 기반 인가 처리
- 비즈니스 로직은 domain에서 처리
- 공통 코드는 module에 위치
- 과도한 계층 분리 지양 (모놀리식 기준 단순 구조 유지)
- 테스트코드는 반드시 필수

## 4. API Conventions
- RESTful 설계 원칙 준수
- 클라이언트 API: `/api/v1/client/**`
- 관리자 API: `/api/v1/admin/**`
- `@RequestMapping` 접두사 사용 금지 (전체 검색 지원 목적)
- 모든 응답은 `ApiResponseUtil` 사용
  ``` java
  return ApiResponseUtil.sendApiResponse(
  HttpStatus.OK,
  "sm.common.success.default",
  "success",
  data,
  null
  );
  ```
## 5. Exception 처리
- 모든 예외는 `CustomException` 사용
  ``` java
  repository.findByXxx()
  .orElseThrow(() -> new CustomException(
  HttpStatus.BAD_REQUEST,
  "project.method.fail.reason",
  "메시지"
  ));
  ```
  에러 코드 형식:{project}.{method}.{success|fail}.{detail}
  예:sm.create_budget.fail.duplicate_period
- `GlobalExceptionHandler`에서 공통 처리

## 7. Entity 설계 규칙
- 모든 테이블 PK는 autoIncrement 사용
- PK 컬럼명은 `{table}_idx` 형식으로 통일
예: - user_idx - budget_idx - memo_idx
- 날짜 타입 필드: `~Date`
- 날짜 + 시간 타입 필드: `~DateTime`
- 예: createdDate, deletedDateTime
- 모든 Entity는 `BaseEntity` 상속

## 9. 인증 처리
- Controller에서 `@AuthenticationPrincipal UserPrincipal` 사용
- Service 호출 시 `userPrincipal.getAuthDto()` 전달
예:
``` java
service.method(userPrincipal.getAuthDto(), request);
```

## 10. Request DTO 규칙
-   모든 Request DTO는 `toEntity()` 메서드 구현 필수
-   연관 엔티티는 파라미터로 전달받아 생성
    예:
``` java
public Entity toEntity(User user) {
  return Entity.builder()
    .user(user)
    .field1(this.field1)
    .build();
}
```

## 11. Validation 규칙

-   Request DTO에 `@Valid` 사용
-   검증 실패는 `GlobalExceptionHandler`에서
    `MethodArgumentNotValidException` 처리
-   자동으로 BAD_REQUEST(400) 반환
-   커스텀 검증은 `jakarta.validation` 어노테이션 사용
  -   @NotNull, @NotBlank, @Min, @Max 등

------------------------------------------------------------------------

## 12. Service 레이어 규칙
-   클래스 레벨: `@Transactional(readOnly = true)`
-   쓰기 메서드에만 `@Transactional` 명시
-   단일 책임 원칙(SRP) 준수
-   클래스명: `{Domain}Service`
예:

``` java
@Service
@Transactional(readOnly = true)
public class SomeService {

    public Data get() { }

    @Transactional
    public void create() { }
}
```

## 14. 전체 플로우 예시
``` java
// Controller
@PostMapping("/api/v1/client/resources")
public ResponseEntity<?> create(
  @AuthenticationPrincipal UserPrincipal userPrincipal,
  @Valid @RequestBody CreateRequest request
) {
  return service.create(userPrincipal.getAuthDto(), request);
}

// Service
@Service
@Transactional(readOnly = true)
public class SomeService {

  @Transactional
  public ResponseEntity<?> create(AuthDto authDto, CreateRequest request) {
    User user = userRepository.findByUserId(authDto.getUserId())
      .orElseThrow(() -> new CustomException(
        HttpStatus.BAD_REQUEST,
        "sm.common.fail.user_not_found",
        "올바르지 않은 사용자 정보입니다."
      ));

    Entity entity = request.toEntity(user);
    Entity saved = repository.save(entity);

    return ApiResponseUtil.sendApiResponse(
      HttpStatus.OK,
      "sm.common.success.default",
      "success",
      EntityResponse.from(saved),
      null
    );
  }
}
```
