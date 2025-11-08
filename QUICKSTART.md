# 🚀 빠른 시작 가이드

바나나 데이트 프로젝트를 5분 안에 시작하세요!

## 1단계: 저장소 클론

```bash
git clone https://github.com/your-org/banana-date.git
cd banana-date
```

## 2단계: 환경 변수 설정

### 최소 설정 (로컬 개발용)

루트 디렉토리에 `.env` 파일 생성:
```bash
# 임시 값으로 시작 (나중에 실제 키로 교체)
KAKAO_CLIENT_ID=temp
KAKAO_CLIENT_SECRET=temp
GOOGLE_CLIENT_ID=temp
GOOGLE_CLIENT_SECRET=temp
```

## 3단계: 개발 환경 실행

### 방법 A: PostgreSQL만 Docker로 (권장)

```bash
# 1. PostgreSQL 실행
docker-compose -f docker-compose.dev.yml up -d

# 2. 백엔드 실행 (새 터미널)
cd backend
./gradlew bootRun

# 3. 프론트엔드 실행 (새 터미널)
cd frontend
npm install
npm run dev
```

### 방법 B: 전체 Docker Compose

```bash
docker-compose up
```

## 4단계: 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html

## 다음 단계

1. **실제 API 키 발급** (OAuth 로그인 사용 시)
   - [Kakao Developers](https://developers.kakao.com/)
   - [Google Cloud Console](https://console.cloud.google.com/)

2. **코드 탐색**
   - 프론트엔드: `frontend/app/page.tsx`
   - 백엔드: `backend/src/main/java/com/bananadate/`

3. **자세한 문서**: [README.md](README.md) 참조

## 문제 해결

### PostgreSQL 연결 실패
```bash
# PostgreSQL이 준비될 때까지 대기
docker-compose -f docker-compose.dev.yml logs -f postgres
```

### 포트 충돌
```bash
# 실행 중인 프로세스 확인
lsof -i :3000  # 프론트엔드
lsof -i :8080  # 백엔드
lsof -i :5432  # PostgreSQL
```

### 권한 문제 (gradlew)
```bash
chmod +x backend/gradlew
```

## 개발 시작!

이제 개발을 시작할 준비가 되었습니다! 🎉
