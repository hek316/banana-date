package com.bananadate.service;

import com.bananadate.dto.PlaceBasicInfo;
import com.bananadate.dto.PlaceCurationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Claude API를 활용하여 장소 정보를 분석하고 데이트 적합도를 평가하는 서비스
 */
@Slf4j
@Service
public class PlaceCurationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-opus-4-20250514";
    private static final int MAX_TOKENS = 1024;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    public PlaceCurationService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${claude.api.key:}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(CLAUDE_API_URL)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    /**
     * 장소 정보를 Claude API로 분석하여 큐레이션 결과를 반환
     *
     * @param placeInfo 카카오 API로 수집한 기본 장소 정보
     * @return 분석된 큐레이션 결과
     */
    public PlaceCurationResult analyzePlaceForDate(PlaceBasicInfo placeInfo) {
        log.info("Analyzing place for date: {}", placeInfo.getPlaceName());

        try {
            // Claude API 호출하여 JSON 응답 받기
            String analysisJson = callClaudeApi(placeInfo);

            // JSON 응답 파싱
            PlaceCurationResult result = parseAnalysisResult(analysisJson, placeInfo);

            log.info("Analysis completed for place: {}, score: {}",
                    placeInfo.getPlaceName(), result.getDateScore());

            return result;

        } catch (Exception e) {
            log.error("Failed to analyze place: {}", placeInfo.getPlaceName(), e);
            throw new RuntimeException("장소 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Claude API를 호출하여 장소 분석 수행
     */
    private String callClaudeApi(PlaceBasicInfo placeInfo) throws JsonProcessingException {
        String prompt = buildPrompt(placeInfo);

        Map<String, Object> requestBody = Map.of(
                "model", CLAUDE_MODEL,
                "max_tokens", MAX_TOKENS,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        try {
            String response = webClient.post()
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests
                                    || throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    new RuntimeException("Claude API 호출 재시도 횟수 초과", retrySignal.failure())))
                    .block();

            return extractContentFromResponse(response);

        } catch (WebClientResponseException e) {
            log.error("Claude API call failed with status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Claude API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Claude API 응답에서 실제 컨텐츠 추출
     */
    private String extractContentFromResponse(String response) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode contentArray = rootNode.path("content");

        if (contentArray.isArray() && contentArray.size() > 0) {
            return contentArray.get(0).path("text").asText();
        }

        throw new RuntimeException("Claude API 응답에서 컨텐츠를 찾을 수 없습니다");
    }

    /**
     * 장소 분석을 위한 프롬프트 생성
     */
    private String buildPrompt(PlaceBasicInfo placeInfo) {
        return String.format("""
                당신은 서울의 데이트 장소를 추천하는 전문가입니다.
                아래 장소 정보를 바탕으로 데이트 적합도를 분석해주세요.

                장소 정보:
                - 이름: %s
                - 카테고리: %s
                - 주소: %s

                다음 형식의 JSON으로만 응답해주세요 (다른 설명 없이 JSON만):
                {
                  "date_score": 1-10 사이의 정수 (데이트 적합도 점수),
                  "mood_tags": ["#태그1", "#태그2", "#태그3"] (최대 3개의 분위기 해시태그),
                  "price_range": "예상 가격대 (예: 10,000-20,000원 또는 무료)",
                  "best_time": "추천 시간대 (예: 저녁 6-9시)",
                  "recommendation": "한 줄 추천 이유 (20자 이내)"
                }

                분석 시 고려사항:
                - 장소 이름, 카테고리, 위치를 종합적으로 고려하여 추론
                - 데이트 분위기, 접근성, 주변 환경 등을 고려
                - 실제 리뷰 데이터가 없으므로 일반적인 특성으로 판단
                """,
                placeInfo.getPlaceName(),
                placeInfo.getCategory(),
                placeInfo.getAddress()
        );
    }

    /**
     * Claude API 응답 JSON을 PlaceCurationResult로 파싱
     */
    private PlaceCurationResult parseAnalysisResult(String analysisJson, PlaceBasicInfo placeInfo)
            throws JsonProcessingException {

        // JSON 추출 (```json ... ``` 형태로 오는 경우 처리)
        String cleanJson = extractJsonFromMarkdown(analysisJson);

        JsonNode rootNode = objectMapper.readTree(cleanJson);

        // mood_tags 파싱
        List<String> moodTags = new ArrayList<>();
        JsonNode moodTagsNode = rootNode.path("mood_tags");
        if (moodTagsNode.isArray()) {
            moodTagsNode.forEach(tag -> moodTags.add(tag.asText()));
        }

        return PlaceCurationResult.builder()
                .dateScore(rootNode.path("date_score").asInt())
                .moodTags(moodTags)
                .priceRange(rootNode.path("price_range").asText())
                .bestTime(rootNode.path("best_time").asText())
                .recommendation(rootNode.path("recommendation").asText())
                .placeInfo(placeInfo)
                .build();
    }

    /**
     * 마크다운 코드 블록에서 JSON 추출
     */
    private String extractJsonFromMarkdown(String text) {
        text = text.trim();

        // ```json ... ``` 형태인 경우
        if (text.startsWith("```json") && text.endsWith("```")) {
            return text.substring(7, text.length() - 3).trim();
        }

        // ``` ... ``` 형태인 경우
        if (text.startsWith("```") && text.endsWith("```")) {
            return text.substring(3, text.length() - 3).trim();
        }

        // 이미 순수 JSON인 경우
        return text;
    }
}
