package com.bananadate.service;

import com.bananadate.dto.KakaoLocalSearchResponse;
import com.bananadate.dto.PlaceCollectionResult;
import com.bananadate.entity.Place;
import com.bananadate.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 장소 수집 서비스
 * 카카오 Local API를 사용하여 서울 주요 상권의 장소를 수집하고 데이터베이스에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceCollectionService {

    private final KakaoLocalApiService kakaoLocalApiService;
    private final PlaceRepository placeRepository;

    // 수집할 지역 (5개)
    private static final List<String> TARGET_LOCATIONS = Arrays.asList(
            "강남역",
            "홍대입구",
            "이태원",
            "성수동",
            "여의도"
    );

    // 수집할 카테고리 (4개)
    private static final List<String> TARGET_CATEGORIES = Arrays.asList(
            "이탈리안",
            "카페",
            "일식",
            "한식당"
    );

    /**
     * 서울 주요 상권의 장소를 수집
     * 5개 지역 × 4개 카테고리 = 20개 쿼리 실행
     *
     * @return 수집 결과
     */
    @Transactional
    public PlaceCollectionResult collectPlaces() {
        log.info("Starting place collection...");
        Instant startTime = Instant.now();

        int collectedCount = 0;
        int skippedCount = 0;
        int totalAttempted = 0;

        for (String location : TARGET_LOCATIONS) {
            for (String category : TARGET_CATEGORIES) {
                String query = location + " " + category;
                log.info("Collecting places for query: {}", query);

                try {
                    // 카카오 API로 장소 검색
                    List<KakaoLocalSearchResponse.Document> documents = kakaoLocalApiService.searchPlacesWithLimit(query);
                    totalAttempted += documents.size();

                    // 각 장소를 DB에 저장
                    for (KakaoLocalSearchResponse.Document doc : documents) {
                        // 중복 확인
                        if (placeRepository.existsByKakaoPlaceId(doc.getId())) {
                            log.debug("Place already exists: {} (ID: {})", doc.getPlaceName(), doc.getId());
                            skippedCount++;
                            continue;
                        }

                        // Place 엔티티로 변환 및 저장
                        Place place = convertToPlace(doc);
                        placeRepository.save(place);
                        collectedCount++;

                        log.debug("Saved place: {} (ID: {})", place.getPlaceName(), place.getKakaoPlaceId());
                    }

                    // Rate limiting: 각 쿼리 사이에 0.5초 대기
                    Thread.sleep(500);

                } catch (Exception e) {
                    log.error("Failed to collect places for query: {}", query, e);
                }
            }
        }

        Instant endTime = Instant.now();
        long elapsedSeconds = Duration.between(startTime, endTime).getSeconds();

        PlaceCollectionResult result = PlaceCollectionResult.builder()
                .collectedCount(collectedCount)
                .skippedCount(skippedCount)
                .totalAttempted(totalAttempted)
                .elapsedTimeSeconds(elapsedSeconds)
                .message(String.format("Successfully collected %d places (%d skipped, %d total attempted) in %d seconds",
                        collectedCount, skippedCount, totalAttempted, elapsedSeconds))
                .build();

        log.info("Place collection completed: {}", result.getMessage());
        return result;
    }

    /**
     * Kakao API 문서를 Place 엔티티로 변환
     *
     * @param doc 카카오 API 문서
     * @return Place 엔티티
     */
    private Place convertToPlace(KakaoLocalSearchResponse.Document doc) {
        return Place.builder()
                .kakaoPlaceId(doc.getId())
                .placeName(doc.getPlaceName())
                .category(doc.getCategoryName())
                .address(doc.getRoadAddressName() != null && !doc.getRoadAddressName().isEmpty()
                        ? doc.getRoadAddressName()
                        : doc.getAddressName())
                .latitude(Double.parseDouble(doc.getY()))
                .longitude(Double.parseDouble(doc.getX()))
                .phone(doc.getPhone())
                .placeUrl(doc.getPlaceUrl())
                // Curation 정보는 나중에 채움 (null로 유지)
                .build();
    }
}
