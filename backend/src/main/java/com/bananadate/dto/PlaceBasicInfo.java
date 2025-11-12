package com.bananadate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 API로부터 수집한 기본 장소 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBasicInfo {

    /**
     * 장소 이름
     */
    private String placeName;

    /**
     * 카테고리 (예: 음식점 > 카페, 관광명소 > 전망대)
     */
    private String category;

    /**
     * 주소
     */
    private String address;

    /**
     * 위도
     */
    private Double latitude;

    /**
     * 경도
     */
    private Double longitude;

    /**
     * 카카오 장소 ID
     */
    private String kakaoPlaceId;
}
