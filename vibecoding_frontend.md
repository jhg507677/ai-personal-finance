# 첫 요청사항 -> 프롬포트 생성 요구
Next 기반 가계부 앱을 위한 프롬프트를 만들어줘
다크모드를 지원할거야
백엔드 코드는 같은 폴더에서 확인할 수 있어
---
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
---
🎯 Phase 2: Transaction 도메인 TDD 구현
사용 시점
Phase 1 완료 후, 가장 핵심이 되는 거래내역 기능부터 구현합니다.
프롬프트
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
- RabbitMQ 연동 완료

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