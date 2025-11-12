package com.bananadate.service;

import com.bananadate.dto.PlaceBasicInfo;
import com.bananadate.dto.PlaceCurationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceCurationService 통합 테스트
 * 실제 Claude API를 호출하므로 API 키가 필요합니다.
 *
 * 테스트 실행 방법:
 * 1. CLAUDE_API_KEY 환경변수 설정
 * 2. ./gradlew test --tests PlaceCurationServiceIntegrationTest
 */
class PlaceCurationServiceIntegrationTest {

    @Test
    void analyzePlaceForDate_실제_Claude_API_호출() {
        // given
        String apiKey = System.getenv("CLAUDE_API_KEY");
        assertThat(apiKey)
                .withFailMessage("CLAUDE_API_KEY 환경변수가 설정되지 않았습니다")
                .isNotNull();

        PlaceCurationService service = new PlaceCurationService(
                WebClient.builder(),
                new ObjectMapper(),
                apiKey
        );

        PlaceBasicInfo placeInfo = PlaceBasicInfo.builder()
                .placeName("남산타워")
                .category("관광명소 > 전망대")
                .address("서울 용산구 남산공원길 105")
                .latitude(37.5511694)
                .longitude(126.9882266)
                .kakaoPlaceId("8223735")
                .build();

        // when
        PlaceCurationResult result = service.analyzePlaceForDate(placeInfo);

        // then
        System.out.println("===== Claude API 분석 결과 =====");
        System.out.println("장소: " + result.getPlaceInfo().getPlaceName());
        System.out.println("데이트 점수: " + result.getDateScore() + "/10");
        System.out.println("분위기 태그: " + result.getMoodTags());
        System.out.println("가격대: " + result.getPriceRange());
        System.out.println("추천 시간: " + result.getBestTime());
        System.out.println("추천 이유: " + result.getRecommendation());
        System.out.println("===============================");

        assertThat(result).isNotNull();
        assertThat(result.getDateScore()).isBetween(1, 10);
        assertThat(result.getMoodTags()).isNotEmpty().hasSizeLessThanOrEqualTo(3);
        assertThat(result.getPriceRange()).isNotBlank();
        assertThat(result.getBestTime()).isNotBlank();
        assertThat(result.getRecommendation()).isNotBlank();
    }

    @Test
    void analyzePlaceForDate_카페_테스트() {
        // given
        String apiKey = System.getenv("CLAUDE_API_KEY");
        assertThat(apiKey)
                .withFailMessage("CLAUDE_API_KEY 환경변수가 설정되지 않았습니다")
                .isNotNull();

        PlaceCurationService service = new PlaceCurationService(
                WebClient.builder(),
                new ObjectMapper(),
                apiKey
        );

        PlaceBasicInfo placeInfo = PlaceBasicInfo.builder()
                .placeName("스타벅스 강남역점")
                .category("음식점 > 카페 > 커피전문점")
                .address("서울 강남구 강남대로 지하 396")
                .latitude(37.4979502)
                .longitude(127.0276368)
                .kakaoPlaceId("26338954")
                .build();

        // when
        PlaceCurationResult result = service.analyzePlaceForDate(placeInfo);

        // then
        System.out.println("===== Claude API 분석 결과 (카페) =====");
        System.out.println("장소: " + result.getPlaceInfo().getPlaceName());
        System.out.println("데이트 점수: " + result.getDateScore() + "/10");
        System.out.println("분위기 태그: " + result.getMoodTags());
        System.out.println("가격대: " + result.getPriceRange());
        System.out.println("추천 시간: " + result.getBestTime());
        System.out.println("추천 이유: " + result.getRecommendation());
        System.out.println("=====================================");

        assertThat(result).isNotNull();
        assertThat(result.getDateScore()).isBetween(1, 10);
    }
}
