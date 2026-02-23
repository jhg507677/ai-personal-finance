# 첫 요청사항 -> 프롬포트 생성 요구
가계부 앱을 위한 프롬프트를 만들어줄수있어? 지금은 먼저 백엔드만
현재 유저쪽은 일부 작업이 된 상태고 가계부 엔티티정의 빼고는 나머진 아무것도 안되어있다고 생각하면돼 테스트 환경에는 h2를 사용할거야 카카오클라우드를 통해서 작업을 할거고 ci, cd 기능, MQ 기능도 넣을거야, 사용자 수는 적을테지만 포토폴리오용이라서 쿠버네티스 기능도 넣을거야 글고 무조건 TDD와 JPA로 구현해야해
각 단계를 나눠서 요청해야해, 그렇지 않으면 토큰이 더 빨리 고갈될거야 나는 클로드 pro 를 사용하고 있어 그거에 맞춰서 나눠줘
------------------------------------------------
🎯 Phase 1: 프로젝트 초기 설정 및 도메인 모델 설계
Spring Boot 기반 가계부 앱 백엔드를 개발하려고 합니다. TDD와 JPA를 필수로 사용해야 합니다.
서버 관련된건 모두 네이버 클라우드 사용할거야,
추후 서비스가 확장되면 메시지큐와 레디스를 도입할것이기 때문에 이거 고려하고 코드를 짜줬으면 좋겠어

## 기술 스택
- Spring Boot 3.x
- Java 17+
- Spring Data JPA
- H2 (테스트용)
- Mysql (운영용)
- JUnit 5, Mockito
- Gradle

## 현재 상태
- User 엔티티 및 인증 기능은 일부 구현됨
- 가계부 관련 기능은 아직 없음

## 요청사항
1. 프로젝트 전체 구조 설계 (패키지 구조)
2. 다음 엔티티들의 상세 설계 (필드, 연관관계, 제약조건):
  - Ledger (거래내역: 수입/지출)
  - Category (카테고리: 식비, 교통비 등)
  - Budget (예산 설정)
  - RecurringTransaction (정기 거래)

3. 각 엔티티의 JPA 애노테이션 포함
4. 엔티티 간 연관관계 설정 (User와의 관계 포함)
5. 공통 기본 클래스 설계 (BaseEntity - createdAt, updatedAt 등)

## 제약사항
- 모든 엔티티는 soft delete 지원 (deleted_at 컬럼)
- 날짜/시간은 LocalDateTime 사용
- 금액은 BigDecimal 사용
- Enum 활용 (거래유형, 결제수단, 예산주기 등)
---------------------------------------------------
🎯 Phase 2: Transaction 도메인 TDD 구현
자 추가 사항 알려줄게
1. 네이밍 규칙
날짜 타입 필드는 ~Date
날짜 + 시간 타입 필드는 ~DateTime
예: createdDate, deletedDateTime

2. API 응답 형식
모든 API 응답 포맷은 기존 UserController의 응답 형식을 기준으로 통일

3. API URI 규칙
클라이언트 API: /api/v1/client/**
관리자 API: /api/v1/admin/**

4. 공유 기능 & 권한 모델
하나의 가계부(또는 리소스)는 최대 4명까지 공유 가능
최초 생성자는 기본적으로 MASTER 권한
초대 링크를 통해 사용자를 초대할 수 있음
초대 시 초대한 사용자가 상대에게 부여할 권한을 선택 가능
MASTER 또는 VIEW

권한 모델 예시:
MASTER: 수정/삭제/공유/권한 부여 가능
VIEW: 조회만 가능

5. 인가(Authorization) 방식
권한 체크는 AOP 기반 인가 처리
Controller 레벨에는 권한 로직을 두지 말 것

6. PK 및 컬럼 네이밍 규칙
모든 테이블 PK는 autoIncrement 사용
컬럼명은 {테이블명}_idx 형식으로 통일
예: user_idx, budget_idx, memo_idx
사용자 도메인의 ID와 혼동되지 않도록 명확히 구분

7. 인증 기능
일반 로그인 + Google OAuth 로그인 지원
구글 로그인 최초 시 자동 회원가입 처리

8. 챗봇 기능 (포트폴리오용 확장 고려)
현재는 서비스 사용법을 안내하는 챗봇 기능 구현
추후 AI 챗봇 도입을 고려한 구조로 설계 (확장 가능하도록 인터페이스 분리)
클라이언트 채팅 API + 관리자 관리 API 분리

9. 관리자 1:1 문의 기능
사용자는 관리자와 1:1 문의 채팅 가능
관리자는 어드민 페이지에서 모든 사용자 문의 내역 조회 가능
관리자 답변 가능

10. JPA @Query 작성 규칙
@Query 사용 시 반드시 Text Block(""") 문법 사용
네이티브 쿼리 또는 JPQL 가독성 위주로 작성

11. 공지사항 게시판 필요(후원 & 서버비 사용 내역 공유)
수익 구조:
광고 수익
카카오페이 송금 링크를 통한 후원
사용자는:서버 비용 사용 내역, 운영 공지를 확인 가능
공지사항 게시판 기능 구현:관리자만 작성/수정/삭제 가능
사용자는 조회만 가능
목적:서버 비용 사용 내역 투명성 확보
개인 프로젝트 포트폴리오용 서비스 운영

가계부 앱의 Transaction(거래내역) 도메인을 TDD 방식으로 구현하겠습니다.

## 이미 완료된 작업
- Transaction 엔티티 정의 완료
- 프로젝트 구조 설계 완료

## 구현할 기능
1. TransactionRepository 테스트 및 구현
  - 기본 CRUD
  - 사용자별 거래내역 조회
  - 날짜 범위 조회
  - 카테고리별 조회
  - 거래 유형(수입/지출)별 조회
  - 복합 조건 검색 (QueryDSL 또는 Specification 활용)

2. TransactionService 테스트 및 구현
  - 거래 생성 (입력값 검증)
  - 거래 수정 (권한 검증 - 본인 거래만)
  - 거래 삭제 (Soft Delete)
  - 거래 상세 조회
  - 거래 목록 조회 (페이징, 필터링, 정렬)

3. TransactionController 테스트 및 구현
  - REST API 엔드포인트
  - DTO 설계 (Request, Response)
  - 입력값 검증 (@Valid)
  - 예외 처리

## TDD 요구사항
- 테스트 먼저 작성 (Red → Green → Refactor)
- Repository: @DataJpaTest + H2
- Service: @ExtendWith(MockitoExtension.class)
- Controller: @WebMvcTest + MockMvc
- 테스트 커버리지 80% 이상

## 추가 요청
- 각 계층별로 테스트 코드 예시도 함께 제공
- 예외 처리 전략 (Custom Exception)
--------------------------------------------------------------------------
🎯 Phase 3: Category 도메인 TDD 구현
사용 시점
Phase 2 완료 후 진행합니다.
프롬프트
가계부 앱의 Category(카테고리) 도메인을 TDD 방식으로 구현하겠습니다.

## 이미 완료된 작업
- Category 엔티티 정의 완료
- Transaction 도메인 완전 구현 완료

## 구현할 기능
1. CategoryRepository 테스트 및 구현
  - 사용자별 카테고리 조회
  - 기본 카테고리 조회 (모든 사용자 공통)
  - 거래 유형별 카테고리 조회 (수입/지출)

2. CategoryService 테스트 및 구현
  - 카테고리 생성
  - 카테고리 수정
  - 카테고리 삭제 (해당 카테고리 사용 중인 거래 확인)
  - 기본 카테고리 초기화 (회원가입 시)

3. CategoryController 테스트 및 구현
  - REST API 구현
  - DTO 설계

## 특별 요구사항
- 기본 카테고리 데이터 초기화 (data.sql 또는 ApplicationRunner)
- 카테고리 삭제 시 연관된 Transaction 처리 전략
- 카테고리 색상 코드 검증 (Hex 코드)

## TDD 원칙 준수
각 테스트를 먼저 작성한 후 구현 코드 작성
------------------------------------------
🎯 Phase 4: Budget 도메인 TDD 구현
사용 시점
Phase 3 완료 후 진행합니다.
프롬프트
가계부 앱의 Budget(예산) 도메인을 TDD 방식으로 구현하겠습니다.

## 이미 완료된 작업
- Budget 엔티티 정의 완료
- Transaction, Category 도메인 완전 구현

## 구현할 기능
1. BudgetRepository 테스트 및 구현
  - 사용자별 예산 조회
  - 기간별 예산 조회 (월별/주별/연별)
  - 카테고리별 예산 조회

2. BudgetService 테스트 및 구현
  - 예산 설정
  - 예산 수정
  - 예산 삭제
  - 예산 사용 현황 계산
    * 해당 기간 내 Transaction 집계
    * 예산 대비 사용률 계산
    * 남은 예산 계산
  - 예산 초과 여부 체크

3. BudgetController 테스트 및 구현
  - REST API 구현
  - DTO 설계

## 특별 요구사항
- 예산 기간 겹침 검증 (같은 카테고리, 같은 기간 중복 불가)
- 예산 통계 로직 (실제 지출과 비교)
- 예산 초과 시 알림 준비 (Phase 6에서 MQ 연동)

## TDD 원칙 준수
특히 예산 계산 로직은 다양한 케이스 테스트 필수

🎯 Phase 5: 통계 및 리포트 기능 TDD 구현
사용 시점
Phase 4 완료 후 진행합니다.
프롬프트
가계부 앱의 Statistics(통계) 기능을 TDD 방식으로 구현하겠습니다.

## 이미 완료된 작업
- Transaction, Category, Budget 도메인 모두 구현 완료

## 구현할 기능
1. StatisticsRepository (Custom Repository)
  - 월별 수입/지출 합계 (Native Query 또는 JPQL)
  - 카테고리별 지출 합계
  - 기간별 지출 트렌드
  - 결제수단별 통계

2. StatisticsService 테스트 및 구현
  - 월별 요약 통계
  - 카테고리별 지출 비율 계산
  - 지출 트렌드 분석 (증가/감소율)
  - 주요 지출 카테고리 Top N

3. StatisticsController 테스트 및 구현
  - GET /api/v1/statistics/monthly
  - GET /api/v1/statistics/category
  - GET /api/v1/statistics/trend
  - DTO 설계 (차트 표시용 데이터 구조)

## 특별 요구사항
- 복잡한 집계 쿼리는 QueryDSL 활용
- 성능 최적화 (인덱스 전략)
- 캐싱 고려 (Spring Cache - 선택사항)

## TDD 원칙 준수
통계 계산 로직의 정확성 검증 중요

🎯 Phase 6: Message Queue 연동 (알림 기능)
사용 시점
Phase 5 완료 후 진행합니다.
프롬프트
가계부 앱에 RabbitMQ를 활용한 비동기 알림 기능을 추가합니다.

## 이미 완료된 작업
- 모든 핵심 도메인 구현 완료

## 구현할 기능
1. RabbitMQ 설정
  - Docker Compose로 로컬 RabbitMQ 환경 구성
  - Exchange, Queue, Binding 설정
  - 메시지 직렬화 설정 (JSON)

2. 이벤트 발행 (Publisher)
  - 예산 초과 이벤트
  - 정기 거래 생성 이벤트
  - Spring Events 활용

3. 이벤트 소비 (Consumer)
  - 알림 메시지 생성
  - 로깅 및 모니터링

4. 테스트
  - RabbitMQ 통합 테스트 (TestContainers 활용)
  - 이벤트 발행/소비 검증

## 특별 요구사항
- 메시지 재처리 전략 (Dead Letter Queue)
- 멱등성 보장
- 카카오 클라우드 MQ 연동 준비 (설정 외부화)

## TDD 원칙
이벤트 발행/소비 로직 단위 테스트

🎯 Phase 7: Docker & Kubernetes 설정
사용 시점
Phase 6 완료 후 진행합니다.
프롬프트
가계부 앱을 Docker 컨테이너화하고 Kubernetes에 배포하기 위한 설정을 작성합니다.

## 이미 완료된 작업
- 모든 백엔드 기능 구현 완료

## 구현할 내용
1. Dockerfile 작성
  - Multi-stage build
  - JDK 17 기반
  - 최적화된 이미지 크기

2. Docker Compose
  - Spring Boot App
  - PostgreSQL
  - RabbitMQ
  - 로컬 개발 환경용

3. Kubernetes Manifests
  - Deployment (Rolling Update 전략)
  - Service (LoadBalancer)
  - ConfigMap (설정 외부화)
  - Secret (민감 정보)
  - HPA (Horizontal Pod Autoscaler)
  - Health Check (Liveness, Readiness Probe)

4. Helm Chart 작성 (선택)

## 특별 요구사항
- 환경별 설정 분리 (dev, prod)
- 리소스 제한 설정 (CPU, Memory)
- 카카오 클라우드 환경 고려

🎯 Phase 8: CI/CD 파이프라인 구축
사용 시점
Phase 7 완료 후 진행합니다.
프롬프트
GitHub Actions를 활용한 CI/CD 파이프라인을 구축합니다.

## 이미 완료된 작업
- 전체 백엔드 구현 완료
- Docker 및 Kubernetes 설정 완료

## 구현할 내용
1. GitHub Actions Workflow
  - 자동 테스트 실행 (JUnit)
  - 테스트 커버리지 리포트
  - Docker 이미지 빌드
  - 카카오 클라우드 컨테이너 레지스트리 푸시
  - Kubernetes 자동 배포

2. 배포 전략
  - Rolling Update
  - Blue-Green 또는 Canary (선택)

3. 모니터링 및 로깅
  - Spring Boot Actuator 설정
  - Prometheus 메트릭 수집
  - 로그 수집 전략

## 특별 요구사항
- 브랜치 전략 (main, develop, feature/*)
- 환경별 배포 (dev, prod)
- Secrets 관리 (GitHub Secrets, Kubernetes Secrets)