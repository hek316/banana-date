package com.bananadate.controller;

import com.bananadate.dto.BatchCurationResult;
import com.bananadate.dto.PlaceCollectionResult;
import com.bananadate.dto.PlaceCurationResult;
import com.bananadate.entity.Place;
import com.bananadate.repository.PlaceRepository;
import com.bananadate.service.PlaceBatchCurationService;
import com.bananadate.service.PlaceCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 장소 수집 및 조회 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceCollectionController {

    private final PlaceCollectionService placeCollectionService;
    private final PlaceBatchCurationService placeBatchCurationService;
    private final PlaceRepository placeRepository;

    /**
     * 서울 주요 상권의 장소 수집 실행
     *
     * @return 수집 결과
     */
    @PostMapping("/collect")
    public ResponseEntity<PlaceCollectionResult> collectPlaces() {
        log.info("Starting place collection via API endpoint");
        PlaceCollectionResult result = placeCollectionService.collectPlaces();
        return ResponseEntity.ok(result);
    }

    /**
     * 수집된 장소 목록 조회
     *
     * @param page     페이지 번호 (0부터 시작)
     * @param size     페이지당 개수
     * @param category 카테고리 필터 (optional)
     * @param curated  큐레이션 상태 필터 (optional): true=큐레이션됨, false=큐레이션안됨
     * @return 장소 목록
     */
    @GetMapping
    public ResponseEntity<Page<Place>> getPlaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean curated) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Place> places;
        if (category != null && !category.isEmpty()) {
            places = placeRepository.findByCategoryContaining(category, pageable);
        } else if (curated != null) {
            if (curated) {
                places = placeRepository.findCuratedPlaces(pageable);
            } else {
                places = placeRepository.findUncuratedPlaces(pageable);
            }
        } else {
            places = placeRepository.findAll(pageable);
        }

        return ResponseEntity.ok(places);
    }

    /**
     * 특정 장소 상세 정보 조회
     *
     * @param id 장소 ID
     * @return 장소 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<Place> getPlaceById(@PathVariable Long id) {
        return placeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 장소 통계 정보 조회
     *
     * @return 통계 정보
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlaceStats() {
        long totalCount = placeRepository.count();
        long curatedCount = placeRepository.countCuratedPlaces();
        long uncuratedCount = placeRepository.countUncuratedPlaces();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("curatedCount", curatedCount);
        stats.put("uncuratedCount", uncuratedCount);
        stats.put("curationRate", totalCount > 0 ? (double) curatedCount / totalCount * 100 : 0);

        return ResponseEntity.ok(stats);
    }

    /**
     * 큐레이션되지 않은 장소를 배치로 큐레이션
     *
     * @param limit 한 번에 처리할 최대 개수 (optional, 지정하지 않으면 전체)
     * @return 배치 큐레이션 결과
     */
    @PostMapping("/curate-all")
    public ResponseEntity<BatchCurationResult> curateAllPlaces(
            @RequestParam(required = false) Integer limit) {
        log.info("Starting batch curation via API endpoint (limit: {})", limit);
        BatchCurationResult result = placeBatchCurationService.curateUncuratedPlaces(limit);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 장소를 큐레이션
     *
     * @param id 장소 ID
     * @return 큐레이션 결과
     */
    @PostMapping("/{id}/curate")
    public ResponseEntity<PlaceCurationResult> curateSinglePlace(@PathVariable Long id) {
        log.info("Curating single place with ID: {}", id);
        PlaceCurationResult result = placeBatchCurationService.curateSinglePlace(id);
        return ResponseEntity.ok(result);
    }
}
