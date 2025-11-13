package com.bananadate.service;

import com.bananadate.dto.BatchCurationResult;
import com.bananadate.dto.PlaceBasicInfo;
import com.bananadate.dto.PlaceCurationResult;
import com.bananadate.entity.Place;
import com.bananadate.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 장소 배치 큐레이션 서비스
 * 큐레이션되지 않은 장소들을 일괄 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceBatchCurationService {

    private final PlaceRepository placeRepository;
    private final PlaceCurationService placeCurationService;

    /**
     * 큐레이션되지 않은 모든 장소를 배치로 큐레이션
     *
     * @param limit 한 번에 처리할 최대 개수 (null이면 전체)
     * @return 배치 큐레이션 결과
     */
    @Transactional
    public BatchCurationResult curateUncuratedPlaces(Integer limit) {
        log.info("Starting batch curation...");
        Instant startTime = Instant.now();

        int successCount = 0;
        int failedCount = 0;

        // 큐레이션되지 않은 장소 목록 조회
        List<Place> uncuratedPlaces = placeRepository.findUncuratedPlaces();

        if (limit != null && limit > 0) {
            uncuratedPlaces = uncuratedPlaces.subList(0, Math.min(limit, uncuratedPlaces.size()));
        }

        int totalProcessed = uncuratedPlaces.size();
        log.info("Found {} uncurated places to process", totalProcessed);

        for (Place place : uncuratedPlaces) {
            try {
                log.info("Curating place: {} (ID: {})", place.getPlaceName(), place.getId());

                // Place를 PlaceBasicInfo로 변환
                PlaceBasicInfo basicInfo = PlaceBasicInfo.builder()
                        .placeName(place.getPlaceName())
                        .category(place.getCategory())
                        .address(place.getAddress())
                        .latitude(place.getLatitude())
                        .longitude(place.getLongitude())
                        .kakaoPlaceId(place.getKakaoPlaceId())
                        .build();

                // Claude API로 큐레이션
                PlaceCurationResult curationResult = placeCurationService.analyzePlaceForDate(basicInfo);

                // 큐레이션 결과를 Place 엔티티에 반영
                place.setDateScore(curationResult.getDateScore());
                place.setMoodTags(curationResult.getMoodTags());
                place.setPriceRange(curationResult.getPriceRange());
                place.setBestTime(curationResult.getBestTime());
                place.setRecommendation(curationResult.getRecommendation());
                place.setCuratedAt(LocalDateTime.now());

                placeRepository.save(place);
                successCount++;

                log.info("Successfully curated place: {} (Score: {})", place.getPlaceName(), place.getDateScore());

            } catch (Exception e) {
                failedCount++;
                log.error("Failed to curate place: {} (ID: {})", place.getPlaceName(), place.getId(), e);
            }
        }

        Instant endTime = Instant.now();
        long elapsedSeconds = Duration.between(startTime, endTime).getSeconds();

        BatchCurationResult result = BatchCurationResult.builder()
                .successCount(successCount)
                .failedCount(failedCount)
                .totalProcessed(totalProcessed)
                .elapsedTimeSeconds(elapsedSeconds)
                .message(String.format("Successfully curated %d places (%d failed, %d total) in %d seconds",
                        successCount, failedCount, totalProcessed, elapsedSeconds))
                .build();

        log.info("Batch curation completed: {}", result.getMessage());
        return result;
    }

    /**
     * 특정 장소를 큐레이션
     *
     * @param placeId 장소 ID
     * @return 큐레이션 결과
     */
    @Transactional
    public PlaceCurationResult curateSinglePlace(Long placeId) {
        log.info("Curating single place with ID: {}", placeId);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found with ID: " + placeId));

        // Place를 PlaceBasicInfo로 변환
        PlaceBasicInfo basicInfo = PlaceBasicInfo.builder()
                .placeName(place.getPlaceName())
                .category(place.getCategory())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .kakaoPlaceId(place.getKakaoPlaceId())
                .build();

        // Claude API로 큐레이션
        PlaceCurationResult curationResult = placeCurationService.analyzePlaceForDate(basicInfo);

        // 큐레이션 결과를 Place 엔티티에 반영
        place.setDateScore(curationResult.getDateScore());
        place.setMoodTags(curationResult.getMoodTags());
        place.setPriceRange(curationResult.getPriceRange());
        place.setBestTime(curationResult.getBestTime());
        place.setRecommendation(curationResult.getRecommendation());
        place.setCuratedAt(LocalDateTime.now());

        placeRepository.save(place);

        log.info("Successfully curated place: {} (Score: {})", place.getPlaceName(), place.getDateScore());

        return curationResult;
    }
}
