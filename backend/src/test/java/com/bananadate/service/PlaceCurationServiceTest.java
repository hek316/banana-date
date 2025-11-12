package com.bananadate.service;

import com.bananadate.dto.PlaceBasicInfo;
import com.bananadate.dto.PlaceCurationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceCurationServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PlaceCurationService placeCurationService;
    private ObjectMapper objectMapper;

    private static final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        placeCurationService = new PlaceCurationService(
                webClientBuilder,
                objectMapper,
                TEST_API_KEY
        );
    }

    @Test
    void analyzePlaceForDate_성공_JSON응답() {
        // given
        PlaceBasicInfo placeInfo = PlaceBasicInfo.builder()
                .placeName("남산타워")
                .category("관광명소 > 전망대")
                .address("서울 용산구 남산공원길 105")
                .latitude(37.5511694)
                .longitude(126.9882266)
                .kakaoPlaceId("8223735")
                .build();

        String mockClaudeResponse = """
                {
                  "content": [
                    {
                      "type": "text",
                      "text": "{\\"date_score\\": 9, \\"mood_tags\\": [\\"#로맨틱\\", \\"#야경명소\\", \\"#기념일\\"], \\"price_range\\": \\"15,000-25,000원\\", \\"best_time\\": \\"저녁 6-9시\\", \\"recommendation\\": \\"서울 전경이 한눈에 보이는 낭만적인 장소\\"}"
                    }
                  ]
                }
                """;

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockClaudeResponse));

        // when
        PlaceCurationResult result = placeCurationService.analyzePlaceForDate(placeInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDateScore()).isEqualTo(9);
        assertThat(result.getMoodTags()).hasSize(3);
        assertThat(result.getMoodTags()).contains("#로맨틱", "#야경명소", "#기념일");
        assertThat(result.getPriceRange()).isEqualTo("15,000-25,000원");
        assertThat(result.getBestTime()).isEqualTo("저녁 6-9시");
        assertThat(result.getRecommendation()).isEqualTo("서울 전경이 한눈에 보이는 낭만적인 장소");
        assertThat(result.getPlaceInfo()).isEqualTo(placeInfo);

        verify(webClient, times(1)).post();
    }

    @Test
    void analyzePlaceForDate_마크다운_코드블록_응답() {
        // given
        PlaceBasicInfo placeInfo = PlaceBasicInfo.builder()
                .placeName("테스트 카페")
                .category("음식점 > 카페")
                .address("서울 강남구")
                .build();

        String mockClaudeResponse = """
                {
                  "content": [
                    {
                      "type": "text",
                      "text": "```json\\n{\\"date_score\\": 7, \\"mood_tags\\": [\\"#조용한\\", \\"#카페\\"], \\"price_range\\": \\"5,000-10,000원\\", \\"best_time\\": \\"오후 2-5시\\", \\"recommendation\\": \\"편안한 분위기의 카페\\"}\\n```"
                    }
                  ]
                }
                """;

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockClaudeResponse));

        // when
        PlaceCurationResult result = placeCurationService.analyzePlaceForDate(placeInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDateScore()).isEqualTo(7);
        assertThat(result.getMoodTags()).hasSize(2);
    }

    @Test
    void analyzePlaceForDate_API호출실패_예외발생() {
        // given
        PlaceBasicInfo placeInfo = PlaceBasicInfo.builder()
                .placeName("테스트 장소")
                .category("음식점")
                .address("서울")
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API 호출 실패")));

        // when & then
        assertThatThrownBy(() -> placeCurationService.analyzePlaceForDate(placeInfo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("장소 분석 중 오류가 발생했습니다");
    }
}
