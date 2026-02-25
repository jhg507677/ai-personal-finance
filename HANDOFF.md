# Handoff Document

## Original Task
AI 개인 가계부 백엔드(Spring Boot) 프로젝트 구현 - Ledger(거래내역) CRUD API 및 전반적인 리팩토링

## Current Status
- **최신 커밋**: `bd6bab4` — `feat: Ledger(거래 내역) CRUD API 구현 (Phase 2)`
- 아직 **커밋되지 않은 변경사항**이 다수 존재함 (아래 상세 목록 참고)
- 전체 도메인(Ledger, Budget, Statistics, User) 구현 완료 상태이나 미커밋 작업 중

---

## What Has Been Tried

### ✅ What Worked
- Ledger CRUD API (생성/조회/수정/삭제) 구현 완료
- QueryDSL 기반 `LedgerRepositoryCustom` / `LedgerRepositoryImpl` 추가
- `PageResponse<T>` 신규 작성 (페이징 응답 공통 래퍼)
- `ListResponse` 삭제 → `PageResponse`로 통합
- `Utils` 유틸 클래스 신규 작성
- `CorsConfig` 신규 작성 (CORS 설정 분리)
- `application-dev.yml` / `application-prod.yml` 환경별 설정 분리
- `ApiResponseUtil`에 헬퍼 메서드 추가
- CLAUDE.md 규칙 정비 (명명 규칙, 응답 포맷, 인증 처리 등)
- 테스트 코드 보강 (LedgerControllerTest, LedgerServiceTest, BudgetServiceTest, StatisticsServiceTest 등)

### ❌ What Didn't Work / 미완료
- 미커밋 변경사항들이 아직 스테이징/커밋되지 않은 상태

---

## Current State of Codebase

### 미커밋 변경 파일 목록
| 상태 | 파일 |
|------|------|
| A (신규) | `.claude/skills/handoff.md` |
| M | `.gitignore` |
| M | `CLAUDE.md` |
| M | `README.md` |
| M | `build.gradle` |
| M | `LedgerController.java` |
| M | `StatisticsController.java` |
| M | `BudgetService.java` |
| M | `BudgetResponse.java` |
| M | `Ledger.java` |
| M | `LedgerRepositoryCustom.java` |
| M | `LedgerRepositoryImpl.java` |
| M | `LedgerService.java` |
| M | `LedgerResponse.java` |
| M | `StatisticsService.java` |
| M | `AddUserRequest.java` |
| M | `User.java` |
| M | `UserRepository.java` |
| M | `UserService.java` |
| A (신규) | `CorsConfig.java` |
| M | `Oauth2UserCustomService.java` |
| M | `ApiResponseUtil.java` |
| D (삭제) | `ListResponse.java` |
| AM | `PageResponse.java` |
| M | `AuthDto.java` |
| M | `AuthService.java` |
| M | `SecurityConfig.java` |
| M | `UserPrincipal.java` |
| AM | `Utils.java` |
| A (신규) | `application-dev.yml` |
| A (신규) | `application-prod.yml` |
| M | `application.yml` |
| M | `LedgerControllerTest.java` |
| M | `LedgerRepositoryTest.java` |
| M | `BudgetServiceTest.java` |
| M | `LedgerServiceTest.java` |
| M | `StatisticsServiceTest.java` |

### 프로젝트 구조
```
src/
├── controller/          # LedgerController, StatisticsController
├── domain/
│   ├── budget/          # BudgetService, BudgetResponse, dto/
│   ├── ledger/          # Ledger, LedgerService, LedgerRepositoryCustom, LedgerRepositoryImpl, dto/
│   ├── statistics/      # StatisticsService
│   └── user/            # User, UserService, UserRepository, AddUserRequest
└── module/
    ├── config/          # CorsConfig, oauth/Oauth2UserCustomService
    ├── response/        # ApiResponseUtil, PageResponse (ListResponse 삭제됨)
    ├── security/        # SecurityConfig, AuthDto, AuthService, UserPrincipal
    └── util/            # Utils
```

---

## Next Steps

1. **미커밋 변경사항 검토 및 커밋**: 현재 변경된 37개 파일을 논리적 단위로 나누어 커밋
2. **테스트 실행 및 확인**: `./gradlew test` 실행 후 전체 테스트 통과 여부 확인
3. **AI 기능 연동**: 가계부 데이터 기반 AI 분석/추천 API 구현 (프로젝트명 "AI Personal Finance"에 맞게)
4. **통계 API 고도화**: `StatisticsService` / `StatisticsController` 기능 완성
5. **프론트엔드 연동 준비**: CORS 설정 최종 확인, API 문서화

---

## Important Context

- **인증**: Google OAuth2 + JWT 방식 사용, `@AuthenticationPrincipal UserPrincipal`
- **응답 포맷**: 반드시 `ApiResponseUtil.sendApiResponse(...)` 사용
- **예외 처리**: `CustomException` + `GlobalExceptionHandler` 사용
- **DB**: H2 (테스트), MySQL (프로덕션)
- **환경 설정**: `application-dev.yml`, `application-prod.yml`로 환경 분리됨
- **API prefix**: 클라이언트 `/api/v1/client/**`, 어드민 `/api/v1/admin/**`
- **명명 규칙**: 날짜 필드 `Date`, 날짜+시간 필드 `~At`, 소프트 삭제 `sDelete()`, 하드 삭제 `hDelete()`
- **ListResponse → PageResponse 변경**: `ListResponse.java` 삭제됨, `PageResponse<T>`로 통합

## Questions/Blockers

- 미커밋 변경사항이 많으므로 커밋 전에 전체 테스트 실행 권장
- AI 분석 기능(어떤 AI 모델/API를 연동할지) 방향 결정 필요
