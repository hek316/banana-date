# 🍌 바나나 데이트 (Banana Date)

서울 지역 기반 데이트 매칭 서비스

## 📋 목차
- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [개발 환경 설정](#개발-환경-설정)
- [배포](#배포)
- [문서](#문서)

## 프로젝트 소개

바나나 데이트는 서울 지역을 기반으로 한 데이트 코스 추천 및 매칭 서비스입니다. AI 기반 개인화 추천과 직관적인 지도 기반 UI로 사용자에게 최적의 데이트 경험을 제공합니다.

### 주요 기능
- 🗺️ 서울시 지도 기반 데이트 코스 추천
- 🤖 AI 기반 개인화 추천
- 💬 실시간 매칭 및 채팅
- 📍 장소 정보 및 리뷰
- 👥 사용자 프로필 및 취향 관리

## 기술 스택

### Frontend
- **Framework**: Next.js 16 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Platform**: Web (Desktop & Mobile Web)

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Authentication**: OAuth2 (Kakao, Google) + JWT

### Infrastructure
- **Container**: Docker & Docker Compose
- **Cloud**: AWS ECS (Fargate)
- **Monitoring**: Sentry, CloudWatch
- **CI/CD**: GitHub Actions

### External APIs
- Kakao Local API (장소 정보)
- Kakao Map API (지도)
- OpenAI GPT-4 / Anthropic Claude (AI 추천)

## 프로젝트 구조

```
banana-date/
├── frontend/                 # Next.js 프론트엔드
│   ├── app/                 # Next.js App Router
│   ├── components/          # React 컴포넌트
│   ├── public/             # 정적 파일
│   ├── Dockerfile          # 프론트엔드 컨테이너
│   └── package.json
│
├── backend/                 # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java 소스 코드
│   │   │   └── resources/  # 설정 파일
│   │   └── test/           # 테스트 코드
│   ├── Dockerfile          # 백엔드 컨테이너
│   ├── build.gradle        # Gradle 빌드 스크립트
│   └── gradlew
│
├── docker-compose.yml       # 전체 스택 실행
├── docker-compose.dev.yml   # 개발 환경 (MySQL만)
├── .env.example            # 환경 변수 예시
├── .gitignore
└── README.md
```

## 시작하기

### 사전 요구사항
- Node.js 20 이상
- Java 21 이상
- Docker & Docker Compose
- Git

### 빠른 시작 (Docker Compose)

1. **저장소 클론**
```bash
git clone https://github.com/your-org/banana-date.git
cd banana-date
```

2. **환경 변수 설정**
```bash
cp .env.example .env
# .env 파일을 열어 필요한 API 키 입력
```

3. **전체 스택 실행**
```bash
docker-compose up
```

4. **접속**
- 프론트엔드: http://localhost:3000
- 백엔드 API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 개발 환경 설정

### 옵션 1: 로컬 개발 (권장)

데이터베이스만 Docker로 실행하고, 프론트/백엔드는 로컬에서 직접 실행합니다. 이 방식은 핫 리로드와 빠른 개발 사이클을 지원합니다.

#### 1. PostgreSQL 실행
```bash
docker-compose -f docker-compose.dev.yml up -d
```

#### 2. 백엔드 실행
```bash
cd backend

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 의존성 다운로드 및 실행
./gradlew bootRun
```

백엔드는 http://localhost:8080 에서 실행됩니다.

#### 3. 프론트엔드 실행
```bash
cd frontend

# 환경 변수 설정
cp .env.local.example .env.local
# .env.local 파일 수정

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

프론트엔드는 http://localhost:3000 에서 실행됩니다.

### 옵션 2: 전체 Docker Compose

```bash
docker-compose up
```

이 방식은 전체 스택을 컨테이너로 실행하지만, 코드 변경 시 컨테이너 재빌드가 필요할 수 있습니다.

### 환경 변수 설정

#### 필수 API 키

1. **카카오 API**
   - [Kakao Developers](https://developers.kakao.com/)에서 애플리케이션 생성
   - REST API 키, JavaScript 키 발급
   - OAuth Redirect URI 설정: `http://localhost:3000/auth/kakao/callback`

2. **Google OAuth**
   - [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
   - OAuth 2.0 클라이언트 ID 생성
   - 승인된 리디렉션 URI 설정: `http://localhost:3000/auth/google/callback`

3. **OpenAI (선택사항)**
   - [OpenAI Platform](https://platform.openai.com/)에서 API 키 발급

#### 환경 변수 파일 설정

**루트 디렉토리 (.env)**
```bash
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

**백엔드 (.env 또는 환경 변수)**
```bash
DB_HOST=localhost
DB_USERNAME=bananadate
DB_PASSWORD=bananadate123
JWT_SECRET=your-secret-key-minimum-256-bits
```

**프론트엔드 (.env.local)**
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_KAKAO_REST_API_KEY=your_key
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_client_id
```

## 배포

### Docker 이미지 빌드

```bash
# 백엔드
cd backend
docker build -t banana-date-backend .

# 프론트엔드
cd frontend
docker build -t banana-date-frontend .
```

### AWS ECS 배포

자세한 배포 가이드는 [docs/deployment.md](docs/deployment.md)를 참조하세요.

## 개발 가이드

### 코드 스타일

#### Backend (Java)
- Google Java Style Guide 준수
- Lombok 사용으로 boilerplate 최소화
- MapStruct를 통한 DTO 변환

#### Frontend (TypeScript)
- ESLint + Prettier 사용
- Airbnb TypeScript Style Guide 기반
- 함수형 컴포넌트 및 Hooks 사용

### Git 브랜치 전략

```
main          # 프로덕션 브랜치
├── develop   # 개발 브랜치
    ├── feature/기능명
    ├── fix/버그명
    └── refactor/리팩토링명
```

### 커밋 메시지 규칙

```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드, 설정 변경
```

## 테스트

### 백엔드 테스트
```bash
cd backend
./gradlew test
```

### 프론트엔드 테스트
```bash
cd frontend
npm test
```

## 문서

- [PRD (Product Requirements Document)](prd.md)
- [API 문서](http://localhost:8080/swagger-ui.html) - 서버 실행 후 접속
- [아키텍처 설계](docs/architecture.md)
- [배포 가이드](docs/deployment.md)

## 트러블슈팅

### 자주 발생하는 문제

**PostgreSQL 연결 실패**
```bash
# PostgreSQL 컨테이너 상태 확인
docker-compose ps

# PostgreSQL 로그 확인
docker-compose logs postgres

# 포트 충돌 확인
lsof -i :5432
```

**프론트엔드 빌드 실패**
```bash
# node_modules 삭제 후 재설치
cd frontend
rm -rf node_modules package-lock.json
npm install
```

**백엔드 빌드 실패**
```bash
# Gradle 캐시 클리어
cd backend
./gradlew clean
./gradlew build --refresh-dependencies
```

## 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 팀

- **Project Lead**: TBD
- **Backend**: TBD
- **Frontend**: TBD
- **DevOps**: TBD

## 문의

프로젝트 관련 문의사항은 이슈를 등록해주세요.
