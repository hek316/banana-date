import type {
  DistrictCollection,
  DistrictFeature,
  Coordinate,
  Coordinates,
  District,
  Bounds,
  ViewBox,
  CenterPoint,
} from '@/types/district';

/**
 * GeoJSON 데이터의 경계 영역을 계산합니다.
 */
export function calculateBounds(features: DistrictFeature[]): Bounds {
  let minLat = Infinity;
  let maxLat = -Infinity;
  let minLng = Infinity;
  let maxLng = -Infinity;

  features.forEach((feature) => {
    feature.geometry.coordinates.forEach((polygon) => {
      polygon.forEach(([lng, lat]) => {
        minLat = Math.min(minLat, lat);
        maxLat = Math.max(maxLat, lat);
        minLng = Math.min(minLng, lng);
        maxLng = Math.max(maxLng, lng);
      });
    });
  });

  return { minLat, maxLat, minLng, maxLng };
}

/**
 * 경도/위도 좌표를 SVG 좌표로 변환합니다.
 * @param lng 경도
 * @param lat 위도
 * @param bounds 지도의 경계 영역
 * @param width SVG 너비
 * @param height SVG 높이
 */
export function projectToSVG(
  lng: number,
  lat: number,
  bounds: Bounds,
  width: number,
  height: number
): [number, number] {
  const { minLat, maxLat, minLng, maxLng } = bounds;

  // 경도/위도를 0-1 범위로 정규화
  const x = (lng - minLng) / (maxLng - minLng);
  // 위도는 Y축이 반대이므로 (1 - y)로 변환
  const y = 1 - (lat - minLat) / (maxLat - minLat);

  return [x * width, y * height];
}

/**
 * GeoJSON 좌표 배열을 SVG path 문자열로 변환합니다.
 */
export function coordinatesToPath(
  coordinates: Coordinates,
  bounds: Bounds,
  width: number,
  height: number
): string {
  const pathCommands: string[] = [];

  coordinates.forEach((polygon, polygonIndex) => {
    polygon.forEach((coord, coordIndex) => {
      const [x, y] = projectToSVG(coord[0], coord[1], bounds, width, height);

      if (coordIndex === 0) {
        // 첫 번째 좌표는 M (Move to)
        pathCommands.push(`M ${x.toFixed(2)} ${y.toFixed(2)}`);
      } else {
        // 나머지 좌표는 L (Line to)
        pathCommands.push(`L ${x.toFixed(2)} ${y.toFixed(2)}`);
      }
    });

    // 경로를 닫습니다 (Z)
    pathCommands.push('Z');
  });

  return pathCommands.join(' ');
}

/**
 * 폴리곤의 중심점을 계산합니다 (무게중심).
 */
export function calculateCentroid(
  coordinates: Coordinates,
  bounds: Bounds,
  width: number,
  height: number
): CenterPoint {
  // 첫 번째 폴리곤의 좌표들을 사용
  const polygon = coordinates[0];
  let sumX = 0;
  let sumY = 0;

  polygon.forEach((coord) => {
    const [x, y] = projectToSVG(coord[0], coord[1], bounds, width, height);
    sumX += x;
    sumY += y;
  });

  return {
    x: sumX / polygon.length,
    y: sumY / polygon.length,
  };
}

/**
 * GeoJSON FeatureCollection을 District 배열로 변환합니다.
 * @param geojson GeoJSON FeatureCollection 데이터
 * @param width SVG 너비 (기본값: 1000)
 * @param height SVG 높이 (기본값: 1000)
 */
export function convertGeoJsonToDistricts(
  geojson: DistrictCollection,
  width = 1000,
  height = 1000
): District[] {
  const bounds = calculateBounds(geojson.features);

  return geojson.features.map((feature) => {
    const { code, name, name_eng } = feature.properties;
    const path = coordinatesToPath(feature.geometry.coordinates, bounds, width, height);
    const center = calculateCentroid(feature.geometry.coordinates, bounds, width, height);

    return {
      code,
      name,
      name_eng,
      path,
      center,
    };
  });
}

/**
 * ViewBox 문자열을 생성합니다.
 */
export function createViewBox(width: number, height: number): string {
  return `0 0 ${width} ${height}`;
}

/**
 * 서울시 구 데이터를 로드하고 변환합니다.
 */
export async function loadSeoulDistricts(): Promise<District[]> {
  try {
    const response = await fetch('/data/seoul-districts.json');
    if (!response.ok) {
      throw new Error(`Failed to load GeoJSON: ${response.statusText}`);
    }

    const geojson: DistrictCollection = await response.json();
    return convertGeoJsonToDistricts(geojson);
  } catch (error) {
    console.error('Error loading Seoul districts:', error);
    throw error;
  }
}
