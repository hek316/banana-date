# Product Requirements Document (PRD)

## 프로젝트 개요
바나나 데이트 - 서울 지역 기반 데이트 매칭 서비스

## 기술 스택

### Frontend
- **Framework**: Next.js (App Router)
- **Language**: TypeScript
- **Platform**: Web (Desktop & Mobile Web)
- **Styling**: CSS Modules / Tailwind CSS / styled-components (선택 필요)
- **State Management**: React Context API / Zustand / Recoil (선택 필요)
- **Map Rendering**: SVG + React 또는 D3.js

### Backend & Infrastructure
- **Container Orchestration**: AWS ECS (Elastic Container Service)
- **Compute**: AWS Fargate (서버리스 컨테이너)
- **Load Balancer**: Application Load Balancer (ALB)
- **Database**: Amazon RDS (PostgreSQL) 또는 Amazon Aurora
- **Cache**: Amazon ElastiCache (Redis)
- **Storage**: Amazon S3
- **CDN**: Amazon CloudFront
- **Container Registry**: Amazon ECR (Elastic Container Registry)
- **Infrastructure as Code**: Terraform
- **CI/CD**: GitHub Actions + Terraform + AWS ECS

### Monitoring & Observability
- **Error Tracking**: Sentry
- **Performance Monitoring**: Sentry Performance
- **Infrastructure Monitoring**: AWS CloudWatch
- **Logging**: CloudWatch Logs
- **Alerting**: CloudWatch Alarms + SNS, Sentry Alerts
- **Metrics & Dashboards**: CloudWatch Dashboards
- **Session Replay**: Sentry Session Replay
- **LLM Observability**: Langfuse

### External APIs & Data Sources
- **장소 정보**:
  - 카카오 로컬 API (Kakao Local API) - Primary
  - 크롤링 기반 무료 데이터 (Fallback/Supplement)
- **지도 데이터**: 서울시 행정구역 GeoJSON (공공데이터)
- **소셜 미디어**:
  - Instagram 크롤링 (위치 태그/해시태그 기반)
- **기타**:
  - 카카오 주소 검색 API
  - 네이버 플레이스 크롤링 (보조)

### AI/LLM
- **LLM Provider**: OpenAI GPT-4 또는 Anthropic Claude
- **Use Cases**:
  - 장소 리뷰 요약 및 분석
  - 인스타그램 포스트 감성 분석
  - 개인화된 장소 추천 생성
- **Observability**: Langfuse

### 개발 우선순위
- Phase 1: 웹 버전 개발 (Desktop + Mobile Web)
- Phase 2: 네이티브 앱 (향후 검토)

## 주요 기능

### 1. 지역 선택 (Map-based Region Selection)

#### 개요
사용자가 선호하는 데이트 지역을 선택할 때, 서울시 지도 기반의 인터랙티브 UI를 제공합니다. 사용자는 지도에서 서울의 각 구(區)를 시각적으로 확인하고 선택할 수 있습니다.

#### 지역 단위
- 서울시를 25개 자치구 단위로 분할
- 선택 가능한 구 목록:
  - 강남구, 강동구, 강북구, 강서구
  - 관악구, 광진구, 구로구, 금천구
  - 노원구, 도봉구, 동대문구, 동작구
  - 마포구, 서대문구, 서초구, 성동구
  - 성북구, 송파구, 양천구, 영등포구
  - 용산구, 은평구, 종로구, 중구, 중랑구

#### UI/UX 요구사항

##### 지도 인터페이스
- 서울시 전체 지도를 SVG 기반으로 렌더링
- 각 구의 경계선이 명확하게 표시됨
- 구 이름이 각 영역 중앙에 표시됨
- Next.js의 Server/Client Component 구조를 활용

##### 인터랙션
- **선택 동작**:
  - 사용자가 지도에서 특정 구를 클릭/탭하면 해당 구가 선택됨
  - 선택된 구는 하이라이트 색상으로 표시 (예: 채워진 색상)
  - 다중 선택 가능 (여러 구를 동시에 선택 가능)

- **선택 해제**:
  - 이미 선택된 구를 다시 클릭/탭하면 선택 해제

- **호버 효과** (Desktop):
  - 마우스 오버 시 해당 구가 강조 표시됨
  - 커서가 포인터로 변경되어 클릭 가능함을 표시

##### 시각적 상태
- **기본 상태**: 흰색 또는 연한 회색 배경
- **호버 상태**: 연한 강조 색상 (예: 연한 파란색)
- **선택 상태**: 진한 강조 색상 (예: 진한 파란색 또는 브랜드 컬러)
- **경계선**: 모든 구의 경계선은 항상 보이도록 표시

##### 추가 기능
- **선택 카운터**: 현재 선택된 구의 개수 표시
- **전체 선택/해제 버튼**:
  - "전체 선택" 버튼으로 모든 구 선택
  - "전체 해제" 버튼으로 모든 선택 취소
- **선택된 지역 목록**: 지도 아래 또는 옆에 선택된 구의 이름을 리스트로 표시

#### 기술적 구현사항 (Next.js + TypeScript)

##### Component 구조
```typescript
// Types
interface District {
  id: string;
  name: string;
  path: string; // SVG path data
}

interface RegionSelectionProps {
  selectedDistricts: string[];
  onDistrictsChange: (districts: string[]) => void;
}

// Components
- SeoulMap (Client Component)
  - DistrictPath (각 구를 렌더링)
  - DistrictLabel (구 이름 표시)
- DistrictSelector (Container Component)
  - SeoulMap
  - SelectionControls (전체 선택/해제)
  - SelectedDistrictsList
```

##### 데이터 관리
- **서울시 GeoJSON 데이터**: `/public/data/seoul-districts.json`
- **타입 정의**: `/src/types/district.ts`
- **지도 상태 관리**: React useState 또는 전역 상태 관리 라이브러리

##### 라우팅
- `/region-selection` - 지역 선택 페이지
- Next.js App Router 활용
- Server Component로 초기 데이터 로드
- Client Component로 인터랙티브 지도 구현

##### 반응형 웹 디자인
- **Mobile Web** (< 768px):
  - 세로 모드에 최적화된 지도 크기
  - 터치 이벤트 최적화
  - 하단에 선택된 지역 목록 표시

- **Tablet** (768px - 1024px):
  - 가로/세로 모두 지원
  - 사이드에 선택 목록 표시 가능

- **Desktop** (> 1024px):
  - 넓은 화면에서 더 큰 지도 표시
  - 호버 효과 활성화
  - 사이드 패널에 선택 정보 표시

##### 접근성 (a11y)
- 키보드 네비게이션 지원 (Tab, Enter, Space)
- ARIA 레이블 추가 (`aria-label`, `aria-selected`)
- 포커스 표시 (outline)
- 색상 대비 비율 준수 (WCAG 2.1 AA)

##### 성능 최적화
- SVG 렌더링 최적화
- React.memo로 불필요한 재렌더링 방지
- 선택 상태 변경 시 부드러운 transition (CSS)
- Next.js Image 컴포넌트 활용 (필요 시)
- 코드 스플리팅 (dynamic import)

#### 사용자 플로우
1. 사용자가 지역 선택 페이지 진입 (`/region-selection`)
2. 서울시 지도 표시 (Server Side에서 초기 데이터 로드)
3. 사용자가 원하는 구를 클릭/탭하여 선택
4. 선택된 구가 시각적으로 하이라이트됨
5. 여러 구를 추가로 선택 가능
6. 선택 완료 후 "다음" 또는 "저장" 버튼 클릭
7. 선택된 지역 정보가 서버로 전송되어 저장됨

#### API 설계

##### Endpoints
```typescript
// POST /api/user/regions
// 사용자의 선호 지역 저장
interface SaveRegionsRequest {
  userId: string;
  districtIds: string[];
}

interface SaveRegionsResponse {
  success: boolean;
  savedRegions: string[];
}

// GET /api/user/regions/{userId}
// 사용자의 선호 지역 조회
interface GetRegionsResponse {
  userId: string;
  districtIds: string[];
}
```

#### 제약사항
- 최소 1개 이상의 구를 선택해야 함
- 선택 가능한 최대 구 개수 제한 (선택사항, 예: 최대 10개)
- 웹 브라우저 호환성: Chrome, Safari, Firefox, Edge (최신 2개 버전)

#### 향후 확장 가능성
- 구 단위보다 더 세밀한 지역 선택 (동 단위)
- 인접 구 자동 추천 기능
- 인기 지역 표시 (다른 사용자들이 많이 선택한 지역)
- 지역별 데이트 스팟 미리보기
- PWA (Progressive Web App) 지원

### 2. 데이트 장소 추천 (Place Recommendations)

#### 개요
선택한 지역을 기반으로 데이트하기 좋은 장소(카페, 레스토랑, 문화시설 등)를 추천합니다. 카카오 로컬 API를 주요 데이터 소스로 활용하고, 크롤링을 통한 보조 데이터로 보완합니다.

#### 데이터 소스 전략

##### Primary: 카카오 로컬 API (Kakao Local API)

**장점**:
- 공식 API로 안정적이고 법적 문제 없음
- 실시간 데이터 (영업시간, 휴무일 등)
- 풍부한 장소 정보 (카테고리, 평점, 사진 등)
- 높은 데이터 정확도
- 무료 쿼터 제공 (일 300,000건)

**활용 API**:
```typescript
// 1. 키워드로 장소 검색
GET https://dapi.kakao.com/v2/local/search/keyword.json

interface KakaoSearchParams {
  query: string;           // 검색 키워드 (예: "강남구 카페")
  category_group_code?: string;  // CE7(카페), FD6(음식점), CT1(문화시설)
  x?: string;             // 중심 경도
  y?: string;             // 중심 위도
  radius?: number;        // 반경(m, 최대 20000)
  rect?: string;          // 사각형 영역
  page?: number;          // 페이지
  size?: number;          // 한 페이지 결과 수 (1-15)
  sort?: 'accuracy' | 'distance';
}

interface KakaoPlace {
  id: string;
  place_name: string;
  category_name: string;
  category_group_code: string;
  phone: string;
  address_name: string;
  road_address_name: string;
  x: string;  // 경도
  y: string;  // 위도
  place_url: string;
  distance?: string;
}

// 2. 카테고리로 장소 검색
GET https://dapi.kakao.com/v2/local/search/category.json

// 3. 주소 검색
GET https://dapi.kakao.com/v2/local/search/address.json
```

**카테고리 그룹**:
- `CE7`: 카페
- `FD6`: 음식점
- `CT1`: 문화시설
- `AT4`: 관광명소
- `PK6`: 주차장

**사용 시나리오**:
1. 사용자가 선택한 구(區)를 기반으로 장소 검색
2. 카테고리별 필터링 (카페, 레스토랑, 문화시설)
3. 거리순 또는 정확도순 정렬
4. 결과를 캐싱하여 API 호출 최소화

**API 키 관리**:
- REST API Key를 AWS Secrets Manager에 저장
- ECS Task에서 환경 변수로 주입
- Rate Limiting 구현 (일 300,000건 제한)

##### Fallback: 크롤링 기반 데이터

**목적**:
- 카카오 API 쿼터 초과 시 대체
- 카카오 API에 없는 추가 정보 수집 (리뷰, 평점 등)
- 데이터 보완 및 검증

**크롤링 대상**:
- 네이버 플레이스
- 카카오맵 (웹 스크래핑)
- 공공데이터 포털 (서울시 열린 데이터 광장)

**기술 스택**:
```typescript
// 크롤링: Puppeteer 또는 Playwright
// 스케줄링: AWS EventBridge + Lambda (또는 ECS Scheduled Task)
// 저장: RDS + S3 (이미지)
```

**크롤링 구현 예시**:
```typescript
// Lambda Function for Crawling
import { chromium } from 'playwright-aws-lambda';

interface CrawledPlace {
  name: string;
  category: string;
  address: string;
  rating?: number;
  reviewCount?: number;
  imageUrls?: string[];
  openingHours?: string;
  priceRange?: string;
}

async function crawlNaverPlace(query: string): Promise<CrawledPlace[]> {
  const browser = await chromium.launch();
  const page = await browser.newPage();

  await page.goto(`https://map.naver.com/v5/search/${encodeURIComponent(query)}`);

  // 장소 목록 추출
  const places = await page.$$eval('.search_item', items => {
    return items.map(item => ({
      name: item.querySelector('.name')?.textContent || '',
      category: item.querySelector('.category')?.textContent || '',
      address: item.querySelector('.address')?.textContent || '',
      rating: parseFloat(item.querySelector('.rating')?.textContent || '0'),
      reviewCount: parseInt(item.querySelector('.review_count')?.textContent || '0'),
    }));
  });

  await browser.close();
  return places;
}
```

**크롤링 스케줄**:
- 매주 1회 실행 (주말 새벽)
- 지역별 인기 장소 업데이트
- 신규 장소 발견 및 추가

**법적 고려사항**:
- `robots.txt` 준수
- 과도한 요청 방지 (Rate Limiting)
- 개인정보 미수집 (장소 정보만)
- 저작권 있는 이미지는 썸네일만 저장하거나 원본 링크 사용

#### 데이터 통합 전략

```
1. 사용자 요청
   ↓
2. Redis 캐시 확인
   ├─ Hit → 캐시 데이터 반환
   └─ Miss → 3단계 진행
   ↓
3. 카카오 API 호출
   ├─ 성공 → 결과 반환 + 캐싱
   ├─ 쿼터 초과 → 4단계 진행
   └─ 에러 → 4단계 진행
   ↓
4. 크롤링 데이터 조회 (RDS)
   ├─ 있음 → 반환
   └─ 없음 → 에러 또는 빈 결과
   ↓
5. 응답 캐싱 (Redis, TTL: 24시간)
```

#### API 설계

##### Endpoints

```typescript
// GET /api/places/search
// 장소 검색
interface PlaceSearchRequest {
  districtId: string;        // 구 ID (예: "gangnam")
  category?: string;         // "cafe" | "restaurant" | "culture"
  keyword?: string;          // 검색 키워드
  page?: number;            // 페이지 (기본: 1)
  size?: number;            // 페이지 크기 (기본: 15)
  sort?: 'distance' | 'rating' | 'popularity';
}

interface PlaceSearchResponse {
  places: Place[];
  pagination: {
    currentPage: number;
    totalPages: number;
    totalCount: number;
  };
  dataSource: 'kakao' | 'crawled' | 'cached';
}

interface Place {
  id: string;
  name: string;
  category: string;
  address: string;
  roadAddress?: string;
  latitude: number;
  longitude: number;
  phone?: string;
  rating?: number;
  reviewCount?: number;
  imageUrl?: string;
  kakaoPlaceUrl?: string;
  openingHours?: string;
  priceRange?: string;
  tags?: string[];          // ["데이트", "분위기좋은", "주차가능"]
}

// GET /api/places/{placeId}
// 장소 상세 정보
interface PlaceDetailResponse extends Place {
  description?: string;
  photos?: string[];
  reviews?: Review[];
  nearbyPlaces?: Place[];
}

// POST /api/places/favorites
// 장소 즐겨찾기 추가
interface AddFavoriteRequest {
  userId: string;
  placeId: string;
}

// GET /api/places/popular
// 인기 장소 조회
interface PopularPlacesRequest {
  districtId?: string;
  category?: string;
  limit?: number;
}
```

#### 데이터베이스 스키마

```sql
-- 장소 테이블
CREATE TABLE places (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kakao_place_id VARCHAR(255) UNIQUE,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  address VARCHAR(500),
  road_address VARCHAR(500),
  latitude DECIMAL(10, 8),
  longitude DECIMAL(11, 8),
  phone VARCHAR(50),
  rating DECIMAL(2, 1),
  review_count INTEGER,
  image_url TEXT,
  kakao_place_url TEXT,
  opening_hours JSONB,
  price_range VARCHAR(50),
  tags TEXT[],
  data_source VARCHAR(50) DEFAULT 'kakao', -- 'kakao' | 'crawled'
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 지역-장소 매핑 테이블
CREATE TABLE district_places (
  district_id VARCHAR(50),
  place_id UUID REFERENCES places(id),
  PRIMARY KEY (district_id, place_id)
);

-- 사용자 즐겨찾기 테이블
CREATE TABLE user_favorite_places (
  user_id UUID,
  place_id UUID REFERENCES places(id),
  created_at TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY (user_id, place_id)
);

-- 인기도 추적 테이블
CREATE TABLE place_analytics (
  place_id UUID REFERENCES places(id),
  date DATE,
  view_count INTEGER DEFAULT 0,
  favorite_count INTEGER DEFAULT 0,
  PRIMARY KEY (place_id, date)
);

-- 인덱스
CREATE INDEX idx_places_category ON places(category);
CREATE INDEX idx_places_location ON places USING GIST(
  point(longitude, latitude)
);
CREATE INDEX idx_places_rating ON places(rating DESC);
```

#### 캐싱 전략

**Redis 캐싱**:
```typescript
// 캐시 키 패턴
const CACHE_KEYS = {
  placeSearch: (params: PlaceSearchRequest) =>
    `places:search:${params.districtId}:${params.category}:${params.page}`,
  placeDetail: (placeId: string) =>
    `places:detail:${placeId}`,
  popularPlaces: (districtId: string, category: string) =>
    `places:popular:${districtId}:${category}`,
};

// TTL 설정
const CACHE_TTL = {
  search: 24 * 60 * 60,      // 24시간
  detail: 12 * 60 * 60,      // 12시간
  popular: 6 * 60 * 60,      // 6시간
};
```

#### Rate Limiting

```typescript
// 카카오 API Rate Limiting
const KAKAO_API_LIMITS = {
  dailyQuota: 300000,
  perSecond: 10,
};

// Redis를 이용한 Rate Limiting
async function checkRateLimit(apiKey: string): Promise<boolean> {
  const dailyKey = `rate:kakao:daily:${new Date().toISOString().split('T')[0]}`;
  const count = await redis.incr(dailyKey);

  if (count === 1) {
    await redis.expire(dailyKey, 86400); // 24시간
  }

  return count <= KAKAO_API_LIMITS.dailyQuota;
}
```

#### 모니터링

**Sentry Custom Metrics**:
```typescript
Sentry.metrics.increment('kakao_api.calls', {
  tags: { endpoint: 'search', category: params.category }
});

Sentry.metrics.increment('crawled_data.usage', {
  tags: { reason: 'quota_exceeded' }
});
```

**CloudWatch Metrics**:
- 카카오 API 호출 수 (일별)
- API 응답 시간
- 캐시 히트율
- 크롤링 데이터 사용 빈도

#### 비용 최적화

1. **캐싱 강화**: Redis로 24시간 캐싱
2. **배치 처리**: 인기 장소는 사전 크롤링하여 DB 저장
3. **Lazy Loading**: 상세 정보는 필요 시에만 로드
4. **CDN 캐싱**: CloudFront로 API 응답 캐싱

### 3. 인스타그램 포스트 연동 (Instagram Integration)

#### 개요
장소와 관련된 인스타그램 포스트를 수집하여 실제 사용자 리뷰와 사진을 제공함으로써 데이터 신빙성을 높이고 생생한 후기를 제공합니다.

#### 데이터 소스 전략

##### Instagram Graph API 제외 이유

**현실적인 제약사항**:
- ❌ **자기 계정만 접근 가능**: 다른 사용자의 공개 포스트 검색 불가
- ❌ **위치/해시태그 검색 불가**: 자신이 소유한 비즈니스 계정 콘텐츠만 조회 가능
- ❌ **일반적인 장소 검색 용도로 부적합**: API가 개인 계정 관리 목적으로 설계됨
- ❌ **엄격한 앱 검수**: Facebook 앱 승인 과정이 매우 까다로움
- ❌ **제한된 권한**: 타인의 공개 콘텐츠에 대한 접근 권한 없음

**결론**: Instagram Graph API는 본 서비스의 목적(장소별 사용자 포스트 수집)에 적합하지 않음

##### Instagram 크롤링 기반 전략 (Primary)

**선택 이유**:
- 위치 태그 및 해시태그 기반 공개 포스트 수집 가능
- 다양한 사용자의 실제 리뷰 및 사진 접근
- 장소별 콘텐츠 검색에 적합
- 공개 데이터만 수집하여 법적 리스크 최소화

**기술 스택**:
```typescript
// 크롤링: Playwright + Proxy
// 스케줄링: Lambda + EventBridge (일 1회)
// 저장: RDS + S3 (이미지)
```

**크롤링 구현**:
```typescript
import { chromium } from 'playwright-aws-lambda';

interface CrawledInstagramPost {
  postUrl: string;
  imageUrls: string[];
  caption: string;
  likes: number;
  comments: number;
  username: string;
  timestamp: Date;
  locationTag?: string;
  hashtags: string[];
}

async function crawlInstagramByLocation(
  placeName: string,
  locationTag?: string
): Promise<CrawledInstagramPost[]> {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0...',
  });
  const page = await context.newPage();

  // 위치 태그가 있으면 위치 페이지, 없으면 해시태그 검색
  const url = locationTag
    ? `https://www.instagram.com/explore/locations/${locationTag}/`
    : `https://www.instagram.com/explore/tags/${encodeURIComponent(placeName)}/`;

  await page.goto(url);
  await page.waitForSelector('article');

  // 포스트 링크 수집
  const postLinks = await page.$$eval('article a', links =>
    links.slice(0, 9).map(link => link.href)
  );

  const posts: CrawledInstagramPost[] = [];

  for (const link of postLinks) {
    await page.goto(link);
    await page.waitForSelector('article');

    const post = await page.evaluate(() => {
      const caption = document.querySelector('h1')?.textContent || '';
      const likes = document.querySelector('section button span')?.textContent || '0';
      const username = document.querySelector('header a')?.textContent || '';
      const timestamp = document.querySelector('time')?.getAttribute('datetime') || '';
      const images = Array.from(document.querySelectorAll('article img')).map(
        img => img.src
      );

      return {
        postUrl: window.location.href,
        imageUrls: images,
        caption,
        likes: parseInt(likes.replace(/,/g, ''), 10),
        username,
        timestamp: new Date(timestamp),
        hashtags: caption.match(/#\w+/g) || [],
      };
    });

    posts.push(post);
  }

  await browser.close();
  return posts;
}
```

**법적 고려사항**:
- Instagram Terms of Service 준수
- robots.txt 확인
- Rate Limiting (요청 간 3-5초 간격)
- 개인정보 보호 (사용자 동의 없이 개인정보 미수집)
- 공개 포스트만 수집

#### 데이터 처리 전략

##### 1. 포스트 수집
```
매일 새벽 2시 (EventBridge)
  ↓
인기 장소 100개 선정 (RDS 쿼리)
  ↓
각 장소별 Instagram 포스트 크롤링
  ↓
중복 제거 (permalink 기준)
  ↓
DB 저장 + S3 이미지 업로드
```

##### 2. LLM 기반 분석 (Langfuse 모니터링)

**OpenAI GPT-4 또는 Anthropic Claude 사용**:

```typescript
import Langfuse from 'langfuse';
import OpenAI from 'openai';

const langfuse = new Langfuse({
  publicKey: process.env.LANGFUSE_PUBLIC_KEY!,
  secretKey: process.env.LANGFUSE_SECRET_KEY!,
});

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY!,
});

interface PostAnalysis {
  sentiment: 'positive' | 'neutral' | 'negative';
  summary: string;
  keyPhrases: string[];
  atmosphere: string[];
  recommendedFor: string[];
}

async function analyzeInstagramPost(
  post: CrawledInstagramPost
): Promise<PostAnalysis> {
  // Langfuse Trace 시작
  const trace = langfuse.trace({
    name: 'instagram-post-analysis',
    metadata: {
      postUrl: post.postUrl,
      username: post.username,
    },
  });

  // Generation 추적
  const generation = trace.generation({
    name: 'analyze-post-caption',
    model: 'gpt-4-turbo',
    input: post.caption,
  });

  const prompt = `
다음은 한 장소에 대한 인스타그램 포스트입니다.
이 포스트를 분석하여 JSON 형식으로 다음 정보를 추출해주세요:

포스트 내용:
${post.caption}

좋아요 수: ${post.likes}
해시태그: ${post.hashtags.join(', ')}

분석 항목:
1. sentiment: positive/neutral/negative
2. summary: 2-3문장 요약
3. keyPhrases: 주요 키워드 3-5개
4. atmosphere: 분위기 키워드 (예: "로맨틱", "활기찬", "조용한")
5. recommendedFor: 추천 대상 (예: "커플", "친구", "가족")

JSON 형식으로만 응답해주세요.
`;

  const response = await openai.chat.completions.create({
    model: 'gpt-4-turbo',
    messages: [{ role: 'user', content: prompt }],
    response_format: { type: 'json_object' },
    temperature: 0.3,
  });

  const analysis = JSON.parse(response.choices[0].message.content!);

  // Generation 완료
  generation.end({
    output: analysis,
    usage: {
      promptTokens: response.usage?.prompt_tokens,
      completionTokens: response.usage?.completion_tokens,
      totalTokens: response.usage?.total_tokens,
    },
  });

  // Trace 완료
  trace.update({
    output: analysis,
  });

  return analysis;
}
```

##### 3. 포스트 신빙성 점수 계산

```typescript
interface CredibilityScore {
  score: number; // 0-100
  factors: {
    accountAge: number;
    followerCount: number;
    engagementRate: number;
    postQuality: number;
  };
}

function calculateCredibility(post: CrawledInstagramPost): CredibilityScore {
  const engagementRate = (post.likes + post.comments) / 1000; // 정규화

  return {
    score: Math.min(
      100,
      engagementRate * 30 + // 참여도
      (post.imageUrls.length > 0 ? 20 : 0) + // 이미지 유무
      (post.caption.length > 50 ? 20 : 0) + // 충분한 설명
      (post.hashtags.length >= 3 ? 15 : 0) + // 해시태그
      15 // 기본 점수
    ),
    factors: {
      accountAge: 0, // 크롤링으로는 얻기 어려움
      followerCount: 0,
      engagementRate: engagementRate,
      postQuality: post.caption.length > 50 ? 80 : 50,
    },
  };
}
```

#### 데이터베이스 스키마

```sql
-- Instagram 포스트 테이블
CREATE TABLE instagram_posts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  place_id UUID REFERENCES places(id),
  post_url VARCHAR(500) UNIQUE NOT NULL,
  permalink VARCHAR(500),
  username VARCHAR(100),
  caption TEXT,
  likes INTEGER DEFAULT 0,
  comments INTEGER DEFAULT 0,
  posted_at TIMESTAMP,
  image_urls TEXT[],
  hashtags TEXT[],
  location_tag VARCHAR(255),

  -- LLM 분석 결과
  sentiment VARCHAR(20),
  summary TEXT,
  key_phrases TEXT[],
  atmosphere TEXT[],
  recommended_for TEXT[],

  -- 신빙성 점수
  credibility_score INTEGER,

  data_source VARCHAR(50) DEFAULT 'crawled', -- 'api' | 'crawled'
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_instagram_posts_place ON instagram_posts(place_id);
CREATE INDEX idx_instagram_posts_sentiment ON instagram_posts(sentiment);
CREATE INDEX idx_instagram_posts_credibility ON instagram_posts(credibility_score DESC);
CREATE INDEX idx_instagram_posts_posted_at ON instagram_posts(posted_at DESC);

-- Full-text search for captions
CREATE INDEX idx_instagram_posts_caption_search ON instagram_posts
  USING gin(to_tsvector('korean', caption));
```

#### API 설계

```typescript
// GET /api/places/{placeId}/instagram
// 장소의 인스타그램 포스트 조회
interface InstagramPostsRequest {
  placeId: string;
  sentiment?: 'positive' | 'neutral' | 'negative';
  minCredibility?: number; // 0-100
  limit?: number;
  sort?: 'recent' | 'popular' | 'credibility';
}

interface InstagramPostsResponse {
  posts: InstagramPost[];
  summary: {
    totalPosts: number;
    averageSentiment: number;
    topKeyPhrases: string[];
    commonAtmosphere: string[];
  };
}

interface InstagramPost {
  id: string;
  postUrl: string;
  username: string;
  caption: string;
  imageUrls: string[];
  likes: number;
  comments: number;
  postedAt: Date;
  analysis: {
    sentiment: string;
    summary: string;
    keyPhrases: string[];
    atmosphere: string[];
    recommendedFor: string[];
  };
  credibilityScore: number;
}

// GET /api/places/{placeId}/summary
// LLM 기반 장소 종합 리뷰 생성
interface PlaceSummaryResponse {
  overallSentiment: string;
  summary: string; // LLM이 생성한 종합 요약
  pros: string[];
  cons: string[];
  bestFor: string[];
  atmosphereScore: {
    romantic: number;
    lively: number;
    quiet: number;
    luxurious: number;
  };
  sourceCount: {
    instagram: number;
    kakao: number;
    naver: number;
  };
}
```

#### Langfuse LLM Observability

##### 설정

```typescript
// langfuse.config.ts
import { Langfuse } from 'langfuse';

export const langfuse = new Langfuse({
  publicKey: process.env.LANGFUSE_PUBLIC_KEY!,
  secretKey: process.env.LANGFUSE_SECRET_KEY!,
  baseUrl: process.env.LANGFUSE_BASE_URL || 'https://cloud.langfuse.com',
});

// Flush on shutdown
process.on('SIGTERM', async () => {
  await langfuse.shutdownAsync();
});
```

##### 추적 항목

**1. Traces (전체 워크플로우)**:
```typescript
const trace = langfuse.trace({
  name: 'place-recommendation',
  userId: user.id,
  metadata: {
    districtId: params.districtId,
    category: params.category,
  },
});
```

**2. Generations (LLM 호출)**:
```typescript
const generation = trace.generation({
  name: 'summarize-reviews',
  model: 'gpt-4-turbo',
  modelParameters: {
    temperature: 0.3,
    maxTokens: 500,
  },
  input: reviews,
});

// ... LLM 호출 ...

generation.end({
  output: summary,
  usage: {
    promptTokens: usage.prompt_tokens,
    completionTokens: usage.completion_tokens,
    totalTokens: usage.total_tokens,
  },
});
```

**3. Scores (품질 평가)**:
```typescript
trace.score({
  name: 'user-feedback',
  value: userRating, // 1-5
  comment: userComment,
});

trace.score({
  name: 'relevance',
  value: relevanceScore, // 0-1
  dataType: 'NUMERIC',
});
```

**4. Spans (중간 단계)**:
```typescript
const span = trace.span({
  name: 'fetch-instagram-posts',
  input: { placeId },
});

// ... 포스트 수집 ...

span.end({
  output: posts,
  metadata: {
    postsCount: posts.length,
    source: 'crawled',
  },
});
```

##### Langfuse Dashboard 활용

**모니터링 지표**:
- LLM 호출 횟수 및 비용
- 평균 레이턴시 (Trace/Generation별)
- 토큰 사용량 (Prompt/Completion)
- 에러율 및 실패 원인
- 사용자 피드백 점수

**최적화**:
- 느린 Trace 식별 및 개선
- 비용이 높은 Generation 최적화
- Prompt 개선 (A/B 테스트)
- 캐싱 전략 수립

##### 비용 최적화

```typescript
// LLM 응답 캐싱
const cacheKey = `llm:summary:${placeId}`;
const cached = await redis.get(cacheKey);

if (cached) {
  return JSON.parse(cached);
}

const summary = await generateSummaryWithLLM(place);
await redis.setex(cacheKey, 86400, JSON.stringify(summary)); // 24시간

return summary;
```

**배치 처리**:
```typescript
// 인기 장소는 매일 새벽 일괄 분석
async function batchAnalyzePlaces() {
  const popularPlaces = await getPopularPlaces(100);

  for (const place of popularPlaces) {
    const posts = await getInstagramPosts(place.id);

    for (const post of posts) {
      // 이미 분석된 포스트는 스킵
      if (post.sentiment) continue;

      const analysis = await analyzeInstagramPost(post);
      await updatePostAnalysis(post.id, analysis);

      // Rate limiting
      await sleep(1000);
    }
  }
}
```

#### 사용자 경험

**장소 상세 페이지**:
```
┌─────────────────────────────────────┐
│  [장소 이름]                         │
│  ★★★★☆ 4.5 (카카오)                │
├─────────────────────────────────────┤
│  📸 Instagram 후기 (32)             │
│  😊 긍정 85% | 😐 중립 10% | 😞 부정 5% │
├─────────────────────────────────────┤
│  [이미지] [이미지] [이미지]           │
│  @username1  @username2  @username3  │
├─────────────────────────────────────┤
│  💡 AI 요약                         │
│  "분위기 좋고 데이트하기 좋은 곳.     │
│   커플들에게 특히 인기가 많으며..."   │
├─────────────────────────────────────┤
│  🏷️ 분위기: 로맨틱, 조용한          │
│  👥 추천: 커플, 친구                 │
└─────────────────────────────────────┘
```

### 4. 인증 및 사용자 관리 (Authentication & User Management)

#### 인증 전략

##### 카카오톡 간편 로그인 (Only)

**선택 이유**:
- 국내 사용자 대부분이 카카오톡 보유
- 간편하고 빠른 로그인 경험
- 추가 회원가입 절차 불필요
- 신뢰성 높은 인증 수단

**다른 인증 수단 제외**:
- 이메일/비밀번호 회원가입 ❌
- 소셜 로그인 (구글, 네이버, 애플) ❌
- 전화번호 인증 ❌

**Kakao Login API**:
```typescript
// 카카오 로그인 SDK
import { KakaoLogin } from '@react-oauth/kakao';

interface KakaoUser {
  id: number;
  kakao_account: {
    profile: {
      nickname: string;
      profile_image_url?: string;
      thumbnail_image_url?: string;
    };
    email?: string;
    age_range?: string;
    gender?: string;
  };
}

// 로그인 플로우
async function loginWithKakao() {
  // 1. 카카오 인증
  const authCode = await Kakao.Auth.authorize({
    redirectUri: `${window.location.origin}/auth/kakao/callback`,
  });

  // 2. 토큰 교환
  const tokenResponse = await fetch('/api/auth/kakao/token', {
    method: 'POST',
    body: JSON.stringify({ code: authCode }),
  });

  const { accessToken, refreshToken } = await tokenResponse.json();

  // 3. 사용자 정보 조회
  const userResponse = await fetch('/api/auth/kakao/user', {
    headers: { Authorization: `Bearer ${accessToken}` },
  });

  const user: KakaoUser = await userResponse.json();

  // 4. JWT 생성 및 세션 저장
  const sessionToken = await createSession(user);
  return sessionToken;
}
```

#### 비로그인 우선 UX (Guest-First Experience)

##### 핵심 원칙
**"모든 메인 기능을 비로그인 상태에서 경험 가능"**

##### 비로그인으로 사용 가능한 기능

**✅ 완전 개방 (로그인 불필요)**:
1. **지역 선택**
   - 서울시 지도에서 구 선택
   - 여러 지역 다중 선택
   - 선택한 지역 저장 (LocalStorage)

2. **장소 검색 및 탐색**
   - 카테고리별 장소 검색 (카페, 레스토랑, 문화시설)
   - 키워드 검색
   - 정렬 및 필터링
   - 장소 목록 보기

3. **장소 상세 정보**
   - 카카오 장소 정보 조회
   - 인스타그램 포스트 보기
   - AI 리뷰 요약 읽기
   - 지도에서 위치 확인
   - 영업시간, 전화번호 등 기본 정보

4. **인기 장소 보기**
   - 지역별 인기 장소 Top 10
   - 트렌딩 장소
   - 카테고리별 추천

5. **공유하기**
   - 장소 URL 복사
   - 카카오톡 공유
   - 링크 공유

**🔒 로그인 필요 (개인화 기능)**:
1. **즐겨찾기 (Favorites)**
   - 장소 저장
   - 저장한 장소 목록 보기
   - 저장한 장소 관리

2. **방문 기록 (Visit History)**
   - 방문한 장소 자동 기록
   - 방문 날짜 기록

3. **개인화 추천**
   - 사용자 취향 기반 장소 추천
   - 맞춤형 큐레이션

4. **리뷰 작성**
   - 장소에 대한 리뷰 작성
   - 평점 남기기

##### 로그인 유도 전략

**1. 부드러운 전환 (Soft Login Prompt)**

```typescript
// 즐겨찾기 버튼 클릭 시
function handleFavoriteClick(placeId: string) {
  if (!isLoggedIn) {
    showLoginModal({
      title: '즐겨찾기를 저장하려면 로그인이 필요해요',
      description: '카카오톡으로 3초만에 시작하기',
      onLogin: async () => {
        await loginWithKakao();
        // 로그인 후 자동으로 즐겨찾기 추가
        await addToFavorites(placeId);
      },
      allowClose: true, // 닫기 가능
    });
  } else {
    await addToFavorites(placeId);
  }
}
```

**2. 로그인 모달 디자인**

```
┌─────────────────────────────────────┐
│              [X 닫기]                │
│                                      │
│         💛 즐겨찾기 저장             │
│                                      │
│   마음에 드는 장소를 저장하고         │
│   나만의 데이트 코스를 만들어보세요   │
│                                      │
│  ┌──────────────────────────────┐  │
│  │  💬 카카오톡으로 3초만에 시작  │  │
│  └──────────────────────────────┘  │
│                                      │
│      간편하고 빠르게 시작하기         │
│                                      │
└─────────────────────────────────────┘
```

**3. 로그인 유도 타이밍**

| 기능 | 비로그인 | 로그인 필요 | 유도 방식 |
|------|---------|-----------|---------|
| 장소 검색 | ✅ | - | - |
| 장소 상세 | ✅ | - | - |
| 즐겨찾기 버튼 클릭 | - | 🔒 | 모달 팝업 |
| 10개 이상 장소 탐색 | ✅ | - | 하단 배너 (부드럽게) |
| 리뷰 작성 버튼 클릭 | - | 🔒 | 모달 팝업 |
| 개인화 추천 페이지 | - | 🔒 | 페이지 전체 |

**4. LocalStorage 활용 (비로그인 상태 유지)**

```typescript
// 비로그인 사용자의 데이터를 LocalStorage에 저장
interface GuestData {
  selectedDistricts: string[];
  recentlyViewedPlaces: string[];
  searchHistory: string[];
  preferences: {
    categories: string[];
  };
}

// 로그인 시 LocalStorage 데이터를 서버로 마이그레이션
async function migrateGuestDataToUser(userId: string) {
  const guestData = localStorage.getItem('guestData');
  if (!guestData) return;

  const data: GuestData = JSON.parse(guestData);

  // 서버로 전송
  await fetch('/api/user/migrate-guest-data', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${sessionToken}`,
    },
    body: JSON.stringify({
      userId,
      guestData: data,
    }),
  });

  // 마이그레이션 후 LocalStorage 정리
  localStorage.removeItem('guestData');
}
```

#### 데이터베이스 스키마

```sql
-- 사용자 테이블
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kakao_id BIGINT UNIQUE NOT NULL,
  nickname VARCHAR(100),
  profile_image_url TEXT,
  email VARCHAR(255),
  age_range VARCHAR(20),
  gender VARCHAR(10),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  last_login_at TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_users_kakao_id ON users(kakao_id);

-- 세션 테이블 (Redis 대신 RDS 사용 시)
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id),
  access_token TEXT NOT NULL,
  refresh_token TEXT,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
```

#### API 설계

```typescript
// POST /api/auth/kakao/login
// 카카오 로그인
interface KakaoLoginRequest {
  code: string; // Authorization code
  redirectUri: string;
}

interface KakaoLoginResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    kakaoId: number;
    nickname: string;
    profileImageUrl?: string;
    email?: string;
  };
}

// POST /api/auth/logout
// 로그아웃
interface LogoutResponse {
  success: boolean;
}

// GET /api/auth/me
// 현재 사용자 정보 조회
interface MeResponse {
  user: {
    id: string;
    nickname: string;
    profileImageUrl?: string;
  } | null;
  isLoggedIn: boolean;
}

// POST /api/user/migrate-guest-data
// 비로그인 → 로그인 데이터 마이그레이션
interface MigrateGuestDataRequest {
  userId: string;
  guestData: {
    selectedDistricts?: string[];
    recentlyViewedPlaces?: string[];
    searchHistory?: string[];
  };
}
```

#### 세션 관리

**Redis 기반 세션**:
```typescript
import { Redis } from 'ioredis';

const redis = new Redis(process.env.REDIS_URL);

// 세션 저장
async function createSession(user: KakaoUser): Promise<string> {
  const sessionId = crypto.randomUUID();
  const sessionData = {
    userId: user.id,
    kakaoId: user.kakao_account.id,
    nickname: user.kakao_account.profile.nickname,
    createdAt: Date.now(),
  };

  // 7일 만료
  await redis.setex(
    `session:${sessionId}`,
    7 * 24 * 60 * 60,
    JSON.stringify(sessionData)
  );

  return sessionId;
}

// 세션 조회
async function getSession(sessionId: string) {
  const data = await redis.get(`session:${sessionId}`);
  return data ? JSON.parse(data) : null;
}

// 세션 삭제
async function deleteSession(sessionId: string) {
  await redis.del(`session:${sessionId}`);
}
```

#### JWT 토큰 전략

```typescript
import jwt from 'jsonwebtoken';

interface JWTPayload {
  userId: string;
  kakaoId: number;
  nickname: string;
}

// Access Token 생성 (1시간)
function generateAccessToken(payload: JWTPayload): string {
  return jwt.sign(payload, process.env.JWT_SECRET!, {
    expiresIn: '1h',
  });
}

// Refresh Token 생성 (7일)
function generateRefreshToken(payload: JWTPayload): string {
  return jwt.sign(payload, process.env.JWT_REFRESH_SECRET!, {
    expiresIn: '7d',
  });
}

// Token 검증
function verifyToken(token: string): JWTPayload | null {
  try {
    return jwt.verify(token, process.env.JWT_SECRET!) as JWTPayload;
  } catch {
    return null;
  }
}
```

#### 사용자 플로우

**1. 비로그인 사용자 플로우**:
```
1. 홈페이지 방문
   ↓
2. 지역 선택 (로그인 불필요)
   ↓
3. 장소 검색 및 탐색 (로그인 불필요)
   ↓
4. 장소 상세 보기 (로그인 불필요)
   ↓
5. [즐겨찾기 버튼 클릭]
   ↓
6. 로그인 모달 팝업 ⚠️
   ├─ "카카오톡으로 3초만에 시작"
   └─ [닫기] 가능 (강요 안함)
```

**2. 로그인 사용자 플로우**:
```
1. 즐겨찾기 버튼 클릭
   ↓
2. 로그인 모달 팝업
   ↓
3. 카카오톡 로그인
   ↓
4. 자동으로 즐겨찾기 추가 ✅
   ↓
5. "저장되었습니다" 토스트 메시지
```

**3. 데이터 마이그레이션 플로우**:
```
1. 비로그인 상태에서 10개 장소 탐색
   ↓ (LocalStorage에 저장)
2. 로그인
   ↓
3. LocalStorage 데이터 자동 마이그레이션
   ├─ 선택한 지역 → 사용자 프로필
   ├─ 최근 본 장소 → 방문 기록
   └─ 검색 기록 → 사용자 선호도
   ↓
4. "저장된 데이터가 계정에 연동되었습니다" 알림
```

#### 보안 고려사항

**1. CSRF 방지**:
```typescript
// SameSite Cookie 설정
response.cookie('sessionId', sessionId, {
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'lax',
  maxAge: 7 * 24 * 60 * 60 * 1000, // 7일
});
```

**2. XSS 방지**:
- Next.js 기본 XSS 보호 활용
- 사용자 입력 sanitize
- CSP (Content Security Policy) 설정

**3. Rate Limiting**:
```typescript
// 로그인 API Rate Limiting
const loginRateLimit = rateLimit({
  windowMs: 15 * 60 * 1000, // 15분
  max: 5, // 최대 5회 시도
  message: '너무 많은 로그인 시도가 있었습니다. 잠시 후 다시 시도해주세요.',
});
```

#### 테스트 시나리오

**비로그인 UX 테스트**:
- [ ] 비로그인 상태로 모든 장소 검색 가능
- [ ] 비로그인 상태로 장소 상세 정보 확인 가능
- [ ] 즐겨찾기 클릭 시 로그인 모달 표시
- [ ] 로그인 모달 닫기 가능
- [ ] LocalStorage에 선택 지역 저장 확인

**로그인 플로우 테스트**:
- [ ] 카카오 로그인 정상 동작
- [ ] JWT 토큰 발급 및 검증
- [ ] 세션 유지 확인 (7일)
- [ ] 로그아웃 정상 동작

**데이터 마이그레이션 테스트**:
- [ ] 비로그인 데이터 LocalStorage 저장
- [ ] 로그인 후 자동 마이그레이션
- [ ] 마이그레이션 후 LocalStorage 정리

## 인프라 아키텍처 (AWS ECS + Terraform)

### 아키텍처 개요

```
User
  ↓
CloudFront (CDN)
  ↓
Application Load Balancer (ALB)
  ↓
ECS Service (Fargate)
  ├── Task 1 (Next.js Container)
  ├── Task 2 (Next.js Container)
  └── Task N (Auto Scaling)
  ↓
├── RDS (PostgreSQL)
├── ElastiCache (Redis)
└── S3 (Static Assets)
```

### AWS 리소스 구성

#### 1. 네트워킹
- **VPC**: 격리된 네트워크 환경
- **Subnets**:
  - Public Subnet (ALB용, 2개 AZ)
  - Private Subnet (ECS Tasks용, 2개 AZ)
  - Database Subnet (RDS용, 2개 AZ)
- **Internet Gateway**: 외부 통신
- **NAT Gateway**: Private subnet의 아웃바운드 통신
- **Security Groups**:
  - ALB Security Group (80, 443 포트)
  - ECS Security Group (ALB에서만 접근)
  - RDS Security Group (ECS에서만 접근)

#### 2. 컨테이너 인프라
- **ECR Repository**: Next.js 애플리케이션 Docker 이미지 저장
- **ECS Cluster**: Fargate 기반 클러스터
- **ECS Task Definition**:
  - Container: Next.js 애플리케이션
  - CPU: 256 (.25 vCPU) 또는 512 (.5 vCPU)
  - Memory: 512MB 또는 1GB
  - Environment Variables: DB 연결 정보, API Keys 등
- **ECS Service**:
  - Desired Count: 2 (최소 가용성)
  - Auto Scaling: CPU/Memory 기반
  - Health Check: ALB health check 연동

#### 3. 로드 밸런싱 & CDN
- **Application Load Balancer**:
  - HTTPS 리스너 (443)
  - HTTP → HTTPS 리다이렉트
  - Target Group: ECS Tasks
  - Health Check: `/api/health`
- **CloudFront**:
  - ALB를 Origin으로 설정
  - Static assets 캐싱
  - HTTPS 강제
  - Custom Domain 지원

#### 4. 데이터베이스 & 캐시
- **RDS (PostgreSQL)**:
  - Multi-AZ 배포 (고가용성)
  - Automated Backup
  - 버전: PostgreSQL 15+
  - Instance Class: db.t4g.micro (개발), db.t4g.small (프로덕션)
- **ElastiCache (Redis)**:
  - 세션 관리
  - API 응답 캐싱
  - Node Type: cache.t4g.micro

#### 5. 스토리지
- **S3 Buckets**:
  - Static Assets (이미지, 문서 등)
  - Terraform State 파일 저장
  - 버킷 정책: CloudFront OAI를 통한 접근만 허용

### Terraform 구조

```
terraform/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   └── prod/
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
├── modules/
│   ├── vpc/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecs/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── alb/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── rds/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── elasticache/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── cloudfront/
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
└── backend.tf
```

### CI/CD 파이프라인 (GitHub Actions)

#### 배포 플로우
```yaml
1. Code Push to GitHub
   ↓
2. GitHub Actions Triggered
   ↓
3. Run Tests (Unit, Integration)
   ↓
4. Build Docker Image
   ↓
5. Push to Amazon ECR
   ↓
6. Update ECS Task Definition
   ↓
7. Deploy to ECS (Rolling Update)
   ↓
8. Health Check
   ↓
9. Rollback if Failed
```

#### GitHub Actions Workflows

**`.github/workflows/deploy.yml`**
```yaml
name: Deploy to AWS ECS

on:
  push:
    branches: [main, develop]

jobs:
  test:
    - Run unit tests
    - Run integration tests
    - Run linting

  build:
    - Build Next.js app
    - Build Docker image
    - Push to ECR

  deploy:
    - Update ECS task definition
    - Deploy to ECS service
    - Wait for deployment
    - Run smoke tests
```

**`.github/workflows/terraform.yml`**
```yaml
name: Terraform Infrastructure

on:
  push:
    paths: ['terraform/**']

jobs:
  terraform:
    - terraform fmt -check
    - terraform validate
    - terraform plan
    - terraform apply (on main branch)
```

### 환경 구성

#### Development (dev)
- **ECS Tasks**: 1개
- **RDS**: db.t4g.micro, Single-AZ
- **ElastiCache**: cache.t4g.micro
- **도메인**: dev.banana-date.com

#### Staging (staging)
- **ECS Tasks**: 2개
- **RDS**: db.t4g.small, Single-AZ
- **ElastiCache**: cache.t4g.small
- **도메인**: staging.banana-date.com

#### Production (prod)
- **ECS Tasks**: 2-10개 (Auto Scaling)
- **RDS**: db.t4g.medium, Multi-AZ
- **ElastiCache**: cache.t4g.medium, Multi-AZ
- **도메인**: www.banana-date.com

### 모니터링 & 로깅

#### CloudWatch
- **Logs**:
  - ECS Container Logs (애플리케이션 로그)
  - ALB Access Logs (HTTP 요청 로그)
  - RDS Logs (슬로우 쿼리, 에러 로그)
  - Lambda Logs (있을 경우)
  - Log Groups 보관 기간: 30일 (dev), 90일 (prod)

- **Metrics**:
  - **ECS Metrics**:
    - CPU/Memory 사용률
    - Task Count (실행 중인 태스크 수)
    - Network In/Out
  - **ALB Metrics**:
    - Request Count
    - Response Time (Latency)
    - Target Response Time
    - HTTP 2xx/4xx/5xx Count
    - Unhealthy Host Count
  - **RDS Metrics**:
    - CPU Utilization
    - Database Connections
    - Read/Write IOPS
    - Free Storage Space
    - Replication Lag (Multi-AZ)
  - **ElastiCache Metrics**:
    - CPU Utilization
    - Cache Hit/Miss Rate
    - Evictions
    - Network Bytes In/Out

- **Alarms** (SNS 알림 연동):
  - **Critical (즉시 대응)**:
    - ECS CPU > 80% (5분 이상)
    - ECS Memory > 85% (5분 이상)
    - ALB 5XX Error Rate > 5% (2분 이상)
    - RDS Connection > 80% of max
    - RDS Free Storage < 10GB
    - All ECS Tasks Unhealthy
  - **Warning (모니터링 필요)**:
    - ALB Response Time > 2s (5분 이상)
    - RDS CPU > 70% (10분 이상)
    - Cache Hit Rate < 70%
    - Disk I/O > 1000 IOPS

- **CloudWatch Dashboards**:
  - **Application Dashboard**:
    - 실시간 Request/Response 차트
    - Error Rate 추이
    - API Endpoint별 Latency
  - **Infrastructure Dashboard**:
    - ECS Task 상태 및 리소스 사용률
    - ALB 헬스 체크 상태
    - RDS 성능 메트릭
  - **Business Metrics Dashboard**:
    - 사용자 활동 (회원가입, 로그인)
    - 지역 선택 통계
    - API 호출 통계

#### Sentry (Error Tracking & Performance Monitoring)

- **Error Tracking**:
  - **Frontend (Next.js)**:
    - JavaScript/TypeScript 런타임 에러 캡처
    - React 컴포넌트 에러 바운더리
    - Unhandled Promise Rejections
    - 사용자 세션 리플레이 (Session Replay)
  - **Backend (API)**:
    - 서버 사이드 에러 캡처
    - 데이터베이스 쿼리 에러
    - 외부 API 호출 실패
    - 미들웨어 에러

- **Performance Monitoring**:
  - **Transaction Tracing**:
    - API 엔드포인트별 응답 시간
    - 데이터베이스 쿼리 성능
    - 외부 서비스 호출 시간
  - **Web Vitals**:
    - LCP (Largest Contentful Paint)
    - FID (First Input Delay)
    - CLS (Cumulative Layout Shift)
    - TTFB (Time to First Byte)
  - **Custom Metrics**:
    - 지도 렌더링 시간
    - 사용자 인터랙션 응답 시간

- **Release Tracking**:
  - Git commit SHA 기반 릴리스 추적
  - 배포별 에러율 비교
  - 새 릴리스 후 에러 급증 감지
  - Source Maps 업로드 (스택 트레이스 원본 코드 매핑)

- **Alerts & Notifications**:
  - **Slack 연동**:
    - Critical 에러 즉시 알림
    - 에러 급증 알림 (10분내 10회 이상)
    - 새로운 타입 에러 발견 시
  - **Email 알림**:
    - 일일/주간 에러 리포트
    - 성능 저하 요약
  - **Alert Rules**:
    - Error Rate > 1% (1분 내)
    - P95 Latency > 3s (5분 평균)
    - 특정 사용자가 반복적으로 에러 경험

- **Issue Management**:
  - 에러 그룹핑 및 우선순위 설정
  - GitHub Issues 자동 생성 연동
  - JIRA 티켓 연동 (선택)
  - 에러 해결 상태 추적 (Resolved/Ignored/Unresolved)

- **User Context**:
  - 사용자 ID, 이메일 추적
  - 브라우저/디바이스 정보
  - 세션 정보 (선택한 지역, 페이지 경로)
  - Custom Tags (환경, 버전, 기능 플래그)

- **Environments**:
  - Development: 모든 에러 수집 (샘플링 100%)
  - Staging: 모든 에러 수집 (샘플링 100%)
  - Production: 에러 100%, 트랜잭션 10% 샘플링

#### 통합 모니터링 전략

```
┌─────────────────────────────────────────────────┐
│              사용자 경험 문제                      │
├─────────────────────────────────────────────────┤
│  Sentry: 프론트엔드 에러, 성능 이슈               │
│  - JavaScript 에러                              │
│  - Web Vitals                                  │
│  - Session Replay                              │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│            애플리케이션 레벨 문제                   │
├─────────────────────────────────────────────────┤
│  Sentry: 백엔드 에러, API 성능                   │
│  - Exception Tracking                          │
│  - Transaction Tracing                         │
│  - Database Query Performance                  │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│            인프라 레벨 문제                        │
├─────────────────────────────────────────────────┤
│  CloudWatch: 리소스 모니터링                     │
│  - ECS CPU/Memory                              │
│  - RDS Performance                             │
│  - ALB Health Check                            │
└─────────────────────────────────────────────────┘
```

#### 모니터링 워크플로우

1. **에러 발생 시**:
   - Sentry가 에러 캡처 및 컨텍스트 수집
   - CloudWatch에 로그 기록
   - Slack으로 즉시 알림 (Critical인 경우)
   - 개발자가 Sentry 대시보드에서 스택 트레이스 확인
   - 필요 시 CloudWatch Logs로 상세 로그 분석

2. **성능 저하 발생 시**:
   - Sentry Performance가 느린 트랜잭션 감지
   - CloudWatch Metrics에서 리소스 사용량 확인
   - 병목 구간 식별 (DB Query? External API? CPU?)
   - 알람 발생 및 담당자 할당

3. **배포 후 모니터링**:
   - Sentry Release로 새 버전 에러율 추적
   - CloudWatch Dashboard로 리소스 사용량 확인
   - 30분간 집중 모니터링
   - 에러율 급증 시 자동 롤백

#### Terraform 설정 예시

```hcl
# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/banana-date-app"
  retention_in_days = var.environment == "prod" ? 90 : 30
}

# CloudWatch Alarms
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "banana-date-ecs-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

# Sentry DSN은 AWS Secrets Manager에 저장
resource "aws_secretsmanager_secret" "sentry_dsn" {
  name = "banana-date/sentry-dsn"
}
```

#### Next.js Sentry 설정

```typescript
// sentry.client.config.ts
import * as Sentry from "@sentry/nextjs";

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  environment: process.env.NEXT_PUBLIC_ENV,
  tracesSampleRate: process.env.NODE_ENV === "production" ? 0.1 : 1.0,
  replaysSessionSampleRate: 0.1,
  replaysOnErrorSampleRate: 1.0,
});

// sentry.server.config.ts
import * as Sentry from "@sentry/nextjs";

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.ENV,
  tracesSampleRate: process.env.NODE_ENV === "production" ? 0.1 : 1.0,
  integrations: [
    new Sentry.Integrations.Postgres(),
  ],
});
```

### 보안

#### Secrets Management
- **AWS Secrets Manager**:
  - Database Credentials
  - API Keys
  - JWT Secret
- ECS Task에서 환경 변수로 주입

#### IAM Roles
- **ECS Task Execution Role**: ECR, CloudWatch Logs 접근
- **ECS Task Role**: S3, RDS, Secrets Manager 접근
- **GitHub Actions Role**: ECR, ECS, Terraform State 접근

#### Network Security
- Private Subnet에 애플리케이션 배치
- Security Group으로 트래픽 제한
- RDS는 ECS에서만 접근 가능
- ALB에만 Public IP 할당

### 비용 최적화

#### Development
- Fargate Spot 사용 (비용 70% 절감)
- Single-AZ 배포
- 야간 자동 스케일 다운

#### Production
- Reserved Capacity (장기 사용 시)
- CloudFront 캐싱으로 ALB 트래픽 감소
- RDS 스토리지 자동 스케일링
- S3 Lifecycle Policy (오래된 로그 삭제)

## 다음 단계

### 디자인
- [ ] 와이어프레임 작성
- [ ] UI/UX 디자인 목업
- [ ] 디자인 시스템 정의 (컬러, 타이포그래피, 컴포넌트)

### 개발
- [ ] Next.js 프로젝트 초기화
- [ ] TypeScript 설정 및 타입 정의
- [ ] Dockerfile 작성 (Next.js 최적화)
- [ ] 서울시 GeoJSON 데이터 확보 및 변환
- [ ] SeoulMap 컴포넌트 개발
- [ ] 지역 선택 페이지 구현
- [ ] 반응형 스타일링
- [ ] API 엔드포인트 개발
- [ ] 데이터베이스 스키마 설계

### 인증 (카카오톡 로그인)
- [ ] 카카오 개발자 계정 생성
- [ ] 카카오 로그인 앱 등록 및 설정
- [ ] Redirect URI 설정
- [ ] 카카오 JavaScript SDK 설치
- [ ] 로그인 버튼 컴포넌트 개발
- [ ] 로그인 모달 UI 구현
- [ ] 카카오 OAuth 콜백 처리 (/auth/kakao/callback)
- [ ] JWT 토큰 생성 및 검증 로직 구현
- [ ] Redis 세션 관리 구현
- [ ] 사용자 테이블 스키마 생성 (users, sessions)
- [ ] 로그아웃 API 구현
- [ ] 현재 사용자 조회 API 구현 (/api/auth/me)
- [ ] LocalStorage 기반 비로그인 데이터 저장 구현
- [ ] 로그인 시 데이터 마이그레이션 API 구현
- [ ] 즐겨찾기/리뷰 작성 시 로그인 유도 모달 구현
- [ ] CSRF, XSS 보안 설정
- [ ] Rate Limiting 구현 (로그인 API)

### External API 통합

#### 카카오 로컬 API
- [ ] 카카오 개발자 계정 생성 및 앱 등록
- [ ] 카카오 REST API 키 발급
- [ ] 카카오 로컬 API 클라이언트 구현
- [ ] 카카오 API Rate Limiting 구현 (Redis)
- [ ] 장소 검색 API 엔드포인트 개발 (/api/places/search)
- [ ] 장소 상세 API 엔드포인트 개발 (/api/places/{id})
- [ ] 인기 장소 API 엔드포인트 개발 (/api/places/popular)
- [ ] Redis 캐싱 레이어 구현
- [ ] 장소 데이터베이스 스키마 생성 (places, district_places 등)
- [ ] Playwright/Puppeteer 네이버 플레이스 크롤링 스크립트 개발
- [ ] Lambda 크롤링 함수 작성 (또는 ECS Scheduled Task)
- [ ] EventBridge 스케줄 설정 (주 1회 크롤링)
- [ ] 크롤링 데이터 저장 로직 구현
- [ ] 카카오 API/크롤링 데이터 통합 로직 구현
- [ ] 장소 즐겨찾기 기능 구현
- [ ] 장소 분석 및 인기도 추적 구현

#### Instagram 크롤링
- [ ] Instagram 포스트 데이터베이스 스키마 생성 (instagram_posts)
- [ ] Playwright Instagram 크롤링 스크립트 개발
- [ ] 위치 태그 기반 포스트 수집 로직 구현
- [ ] 해시태그 기반 포스트 수집 로직 구현
- [ ] Lambda 크롤링 함수 작성 (일 1회 실행)
- [ ] EventBridge 스케줄 설정 (매일 새벽 2시)
- [ ] Proxy 설정 (크롤링 안정성)
- [ ] Rate Limiting 구현 (요청 간 3-5초 간격)
- [ ] S3에 인스타그램 이미지 저장 로직 구현
- [ ] 포스트 신빙성 점수 계산 로직 구현
- [ ] Instagram 포스트 조회 API 엔드포인트 개발 (/api/places/{id}/instagram)
- [ ] 크롤링 에러 핸들링 및 재시도 로직 구현

#### LLM & AI 기능
- [ ] OpenAI 또는 Anthropic API 키 발급
- [ ] Langfuse 계정 생성 및 프로젝트 설정
- [ ] Langfuse SDK 설치 및 설정
- [ ] Instagram 포스트 감성 분석 구현
- [ ] 리뷰 요약 생성 기능 구현
- [ ] 장소 종합 분석 API 구현 (/api/places/{id}/summary)
- [ ] LLM 응답 캐싱 전략 구현 (Redis)
- [ ] 배치 분석 스크립트 작성 (인기 장소 일괄 분석)
- [ ] Langfuse Trace/Generation/Score 구현
- [ ] Prompt 최적화 및 A/B 테스트 설정

### 인프라 (Terraform)
- [ ] AWS 계정 설정 및 IAM 사용자 생성
- [ ] Terraform 백엔드 설정 (S3 + DynamoDB)
- [ ] VPC 모듈 작성
- [ ] ECS 클러스터 및 서비스 모듈 작성
- [ ] ALB 모듈 작성
- [ ] RDS 모듈 작성
- [ ] ElastiCache 모듈 작성
- [ ] CloudFront 모듈 작성
- [ ] Security Groups 설정
- [ ] IAM Roles 및 Policies 생성
- [ ] Secrets Manager 설정
- [ ] dev/staging/prod 환경별 variables 설정

### CI/CD
- [ ] GitHub Actions workflow 작성 (.github/workflows/deploy.yml)
- [ ] GitHub Actions workflow 작성 (.github/workflows/terraform.yml)
- [ ] GitHub Secrets 설정 (AWS Credentials)
- [ ] ECR Repository 생성
- [ ] Docker 이미지 빌드 및 푸시 자동화
- [ ] ECS 배포 자동화
- [ ] Rollback 전략 구현

### 테스트
- [ ] 단위 테스트 (Jest + React Testing Library)
- [ ] E2E 테스트 (Playwright 또는 Cypress)
- [ ] 접근성 테스트
- [ ] 크로스 브라우저 테스트
- [ ] 로드 테스트 (k6 또는 Artillery)

### 모니터링 & 보안

#### Application Monitoring
- [ ] Sentry 계정 생성 및 프로젝트 설정
- [ ] Sentry Next.js SDK 설치 및 설정
- [ ] Sentry Source Maps 업로드 자동화
- [ ] Sentry Release Tracking 설정
- [ ] Sentry Slack/Email 알림 연동
- [ ] Sentry Custom Metrics 설정 (카카오 API 호출, 크롤링 사용 등)

#### LLM Observability
- [ ] Langfuse 대시보드 설정
- [ ] LLM 호출 비용 모니터링 설정
- [ ] 토큰 사용량 추적 및 알림
- [ ] Prompt 성능 분석 (레이턴시, 품질)
- [ ] 사용자 피드백 Score 수집 및 분석
- [ ] Langfuse Slack 알림 연동 (비용 임계값 초과 시)

#### Infrastructure Monitoring
- [ ] CloudWatch Log Groups 생성
- [ ] CloudWatch Dashboards 설정 (Application/Infrastructure/Business)
- [ ] CloudWatch Alarms 설정 (Critical/Warning)
- [ ] CloudWatch Metrics 설정 (카카오 API 호출수, 캐시 히트율, LLM 호출수 등)
- [ ] SNS Topic 생성 및 구독 설정 (Slack, Email)
- [ ] Secrets Manager에 API 키 저장 (Sentry DSN, 카카오 API, OpenAI API, Langfuse Keys)
- [ ] WAF 설정 (Optional)
- [ ] SSL/TLS 인증서 (ACM)
- [ ] 로그 보관 정책 설정
