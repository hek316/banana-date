package com.bananadate.service;

import com.bananadate.dto.KakaoLocalSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * 카카오 Local API 연동 서비스
 */
@Slf4j
@Service
public class KakaoLocalApiService {

    private final WebClient webClient;
    private final String apiKey;

    private static final String KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final int PAGE_SIZE = 15; // 카카오 API 한 페이지당 최대 15개
    private static final int MAX_RESULTS_PER_QUERY = 25; // 각 쿼리당 수집할 최대 개수

    public KakaoLocalApiService(
            WebClient.Builder webClientBuilder,
            @Value("${kakao.api.rest-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(KAKAO_LOCAL_API_URL)
                .build();
        this.apiKey = apiKey;
    }

    /**
     * 키워드로 장소 검색
     *
     * @param query 검색 키워드
     * @param page  페이지 번호 (1부터 시작, 최대 45)
     * @param size  한 페이지당 문서 수 (1-15)
     * @return 검색 결과
     */
    public KakaoLocalSearchResponse searchPlaces(String query, int page, int size) {
        log.info("Searching places with query: {}, page: {}, size: {}", query, page, size);

        try {
            KakaoLocalSearchResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("query", query)
                            .queryParam("page", page)
                            .queryParam("size", Math.min(size, PAGE_SIZE))
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .bodyToMono(KakaoLocalSearchResponse.class)
                    .block();

            if (response != null && response.getDocuments() != null) {
                log.info("Found {} places for query: {}", response.getDocuments().size(), query);
            }

            return response;

        } catch (WebClientResponseException e) {
            log.error("Kakao API call failed with status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("카카오 Local API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 키워드로 장소를 최대 개수만큼 수집
     *
     * @param query 검색 키워드
     * @return 수집된 장소 목록
     */
    public List<KakaoLocalSearchResponse.Document> searchPlacesWithLimit(String query) {
        List<KakaoLocalSearchResponse.Document> allDocuments = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore && allDocuments.size() < MAX_RESULTS_PER_QUERY) {
            KakaoLocalSearchResponse response = searchPlaces(query, page, PAGE_SIZE);

            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                break;
            }

            // 최대 개수까지만 추가
            int remainingCount = MAX_RESULTS_PER_QUERY - allDocuments.size();
            List<KakaoLocalSearchResponse.Document> documents = response.getDocuments();
            int countToAdd = Math.min(documents.size(), remainingCount);

            allDocuments.addAll(documents.subList(0, countToAdd));

            // 마지막 페이지이거나 충분히 수집했으면 종료
            hasMore = response.getMeta() != null
                    && !response.getMeta().getIsEnd()
                    && allDocuments.size() < MAX_RESULTS_PER_QUERY;

            page++;

            // Rate limiting: 0.5초 대기
            if (hasMore) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Sleep interrupted", e);
                    break;
                }
            }
        }

        log.info("Collected {} places for query: {}", allDocuments.size(), query);
        return allDocuments;
    }
}
