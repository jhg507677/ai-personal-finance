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

## 11-1. Update Request DTO 규칙

-   **Entity와 비교하여 필수 필드만 `@NotNull` 추가**
-   필드별 추가 검증 (`@Positive`, `@Size` 등)
-   선택 필드는 null 체크 처리
    예:
    ``` java
    @Getter
    public class BudgetUpdateRequest {
      // 필수 필드 (Entity: nullable = false)
      @NotNull(message = "예산 이름은 필수입니다")
      @Size(min = 1, max = 100)
      private String name;

      @NotNull(message = "예산 금액은 필수입니다")
      @Positive
      private BigDecimal amount;

      @NotNull(message = "활성 상태는 필수입니다")
      private Boolean isActive;

      // 선택 필드 (null 가능)
      private Category category;

      @Positive
      private BigDecimal alertThreshold;
    }
    ```

-   Entity의 `update()` 메서드: 모든 필드 직접 할당
    - 필수 필드: 항상 할당
    - 선택 필드: null이 오면 null로 설정 (값 삭제 의미)
    ``` java
    public void update(BudgetUpdateRequest request) {
      this.name = request.getName();
      this.amount = request.getAmount();
      this.isActive = request.getIsActive();

      // 선택 필드도 직접 할당 (null이면 값 삭제)
      this.category = request.getCategory();
      this.alertThreshold = request.getAlertThreshold();
    }
    ```

------------------------------------------------------------------------

## 11-2. API Response 헬퍼 메서드

-   성공 응답은 `sendApiOK()` 헬퍼 메서드 사용
-   중복 코드 제거 (5줄 → 1줄)
-   static import 사용으로 코드 간소화
    예:
    ``` java
    // Before: 5줄 (중복)
    return ApiResponseUtil.sendApiResponse(
        HttpStatus.OK,
        "sm.common.success.default",
        "success",
        BudgetResponse.from(budget),
        null);

    // After: 1줄 (간결)
    return sendApiOK(BudgetResponse.from(budget));
    ```

-   ApiResponseUtil.java 에 정의:
    ``` java
    public static <T> ResponseEntity<?> sendApiOK(T data) {
      return sendApiResponse(
          HttpStatus.OK,
          "sm.common.success.default",
          "success",
          data,
          null);
    }
    ```

-   Service에서 static import:
    ``` java
    import static com.codingcat.aipersonalfinance.module.response.ApiResponseUtil.sendApiOK;
    ```

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

### 14-1. Create (POST)
``` java
// Controller
@PostMapping("/api/v1/client/resources")
public ResponseEntity<?> create(
  @AuthenticationPrincipal UserPrincipal userPrincipal,
  @Valid @RequestBody CreateRequest request
) {
  return service.create(userPrincipal.getAuthDto(), request);
}

// Request DTO
@Getter
public class CreateRequest {
  @NotNull(message = "이름은 필수입니다")
  @Size(min = 1, max = 100)
  private String name;

  @NotNull(message = "금액은 필수입니다")
  @Positive
  private BigDecimal amount;

  public Entity toEntity(User user) {
    return Entity.builder()
        .user(user)
        .name(this.name)
        .amount(this.amount)
        .build();
  }
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

    return sendApiOK(EntityResponse.from(saved));
  }
}
```

### 14-2. Update (PUT)
``` java
// Controller
@PutMapping("/api/v1/client/resources/{id}")
public ResponseEntity<?> update(
  @AuthenticationPrincipal UserPrincipal userPrincipal,
  @PathVariable Long id,
  @Valid @RequestBody UpdateRequest request
) {
  return service.update(userPrincipal.getAuthDto(), id, request);
}

// Request DTO (모든 필드 필수)
@Getter
public class UpdateRequest {
  @NotNull(message = "이름은 필수입니다")
  @Size(min = 1, max = 100)
  private String name;

  @NotNull(message = "금액은 필수입니다")
  @Positive
  private BigDecimal amount;
  // ... 모든 필드에 @NotNull
}

// Entity
public void update(UpdateRequest request) {
  this.name = request.getName();
  this.amount = request.getAmount();
  // ... 모든 필드 직접 할당
}

// Service
@Transactional
public ResponseEntity<?> update(AuthDto authDto, Long id, UpdateRequest request) {
  Entity entity = findEntityById(id);
  validateOwnership(authDto.getUserId(), entity);
  entity.update(request);
  return sendApiOK(EntityResponse.from(entity));
}
```
