package com.bananadate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Claude API로 분석한 장소 큐레이션 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCurationResult {

    /**
     * 데이트 적합도 점수 (1-10점)
     */
    private Integer dateScore;

    /**
     * 분위기 해시태그 (최대 3개)
     * 예: ["#로맨틱", "#조용한", "#야경명소"]
     */
    private List<String> moodTags;

    /**
     * 1인당 예상 가격대
     * 예: "10,000-20,000원", "무료"
     */
    private String priceRange;

    /**
     * 추천 시간대
     * 예: "저녁 6-9시", "오후 2-5시"
     */
    private String bestTime;

    /**
     * 한 줄 추천 이유
     * 예: "야경이 아름다운 루프탑 카페"
     */
    private String recommendation;

    /**
     * 원본 장소 정보
     */
    private PlaceBasicInfo placeInfo;
}
