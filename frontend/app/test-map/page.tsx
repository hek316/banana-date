'use client';

import { useEffect, useState } from 'react';
import { loadSeoulDistricts } from '@/utils/geoJsonToSvg';
import type { District } from '@/types/district';

export default function TestMapPage() {
  const [districts, setDistricts] = useState<District[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDistrict, setSelectedDistrict] = useState<string | null>(null);

  useEffect(() => {
    loadSeoulDistricts()
      .then((data) => {
        setDistricts(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-red-500">Error: {error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">서울시 구 지도 테스트</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* 지도 영역 */}
        <div className="border rounded-lg p-4 bg-gray-50">
          <h2 className="text-xl font-semibold mb-4">서울시 25개 구</h2>
          <svg
            viewBox="0 0 1000 1000"
            className="w-full h-auto border bg-white"
            style={{ maxHeight: '600px' }}
          >
            {districts.map((district) => (
              <g key={district.code}>
                {/* 구 경계선 */}
                <path
                  d={district.path}
                  fill={selectedDistrict === district.code ? '#3b82f6' : '#e5e7eb'}
                  stroke="#374151"
                  strokeWidth="1"
                  className="cursor-pointer hover:fill-blue-200 transition-colors"
                  onClick={() => setSelectedDistrict(district.code)}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.fill = '#93c5fd';
                  }}
                  onMouseLeave={(e) => {
                    if (selectedDistrict !== district.code) {
                      e.currentTarget.style.fill = '#e5e7eb';
                    }
                  }}
                />
                {/* 구 이름 */}
                <text
                  x={district.center.x}
                  y={district.center.y}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  className="text-xs font-medium pointer-events-none"
                  fill="#1f2937"
                >
                  {district.name}
                </text>
              </g>
            ))}
          </svg>
        </div>

        {/* 구 목록 */}
        <div className="border rounded-lg p-4 bg-gray-50">
          <h2 className="text-xl font-semibold mb-4">구 목록 ({districts.length}개)</h2>
          <div className="space-y-2 max-h-[600px] overflow-y-auto">
            {districts.map((district) => (
              <div
                key={district.code}
                className={`p-3 rounded cursor-pointer transition-colors ${
                  selectedDistrict === district.code
                    ? 'bg-blue-500 text-white'
                    : 'bg-white hover:bg-blue-50'
                }`}
                onClick={() => setSelectedDistrict(district.code)}
              >
                <div className="font-semibold">{district.name}</div>
                <div className="text-sm opacity-75">
                  {district.name_eng} (코드: {district.code})
                </div>
                <div className="text-xs opacity-60 mt-1">
                  중심점: ({district.center.x.toFixed(1)}, {district.center.y.toFixed(1)})
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 선택된 구 정보 */}
      {selectedDistrict && (
        <div className="mt-8 p-6 border rounded-lg bg-blue-50">
          <h2 className="text-xl font-semibold mb-2">선택된 구</h2>
          {districts
            .filter((d) => d.code === selectedDistrict)
            .map((district) => (
              <div key={district.code}>
                <p className="text-lg font-medium">{district.name}</p>
                <p className="text-sm text-gray-600">{district.name_eng}</p>
                <p className="text-sm text-gray-600">코드: {district.code}</p>
                <p className="text-xs text-gray-500 mt-2">
                  SVG Path 길이: {district.path.length} characters
                </p>
              </div>
            ))}
        </div>
      )}
    </div>
  );
}
