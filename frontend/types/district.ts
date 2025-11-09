/**
 * GeoJSON 타입 정의
 */

// GeoJSON 좌표 타입 (경도, 위도)
export type Coordinate = [number, number];

// GeoJSON Polygon 좌표 타입 (여러 개의 좌표 배열)
export type Coordinates = Coordinate[][];

// GeoJSON Geometry 타입
export interface Geometry {
  type: 'Polygon';
  coordinates: Coordinates;
}

// 구 속성 정보
export interface DistrictProperties {
  code: string;        // 행정구역 코드
  name: string;        // 한글 구 이름 (예: "강남구")
  name_eng: string;    // 영문 구 이름 (예: "Gangnam-gu")
  base_year: string;   // 기준 년도
}

// GeoJSON Feature (개별 구)
export interface DistrictFeature {
  type: 'Feature';
  properties: DistrictProperties;
  geometry: Geometry;
}

// GeoJSON FeatureCollection (전체 구 데이터)
export interface DistrictCollection {
  type: 'FeatureCollection';
  features: DistrictFeature[];
}

/**
 * SVG 변환을 위한 타입 정의
 */

// 구의 중심점 좌표
export interface CenterPoint {
  x: number;
  y: number;
}

// 변환된 구 데이터 (SVG용)
export interface District {
  code: string;
  name: string;
  name_eng: string;
  path: string;          // SVG path 문자열
  center: CenterPoint;   // 구 이름 표시를 위한 중심점
}

/**
 * 지도 관련 타입 정의
 */

// 지도의 경계 영역
export interface Bounds {
  minLat: number;
  maxLat: number;
  minLng: number;
  maxLng: number;
}

// SVG 뷰포트 설정
export interface ViewBox {
  x: number;
  y: number;
  width: number;
  height: number;
}
