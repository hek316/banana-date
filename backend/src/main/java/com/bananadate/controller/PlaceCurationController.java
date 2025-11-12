package com.bananadate.controller;

import com.bananadate.dto.PlaceBasicInfo;
import com.bananadate.dto.PlaceCurationResult;
import com.bananadate.service.PlaceCurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장소 큐레이션 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Place Curation", description = "장소 큐레이션 API")
public class PlaceCurationController {

    private final PlaceCurationService placeCurationService;

    /**
     * 장소 정보를 분석하여 데이트 적합도 평가
     *
     * @param placeInfo 카카오 API로 수집한 기본 장소 정보
     * @return 큐레이션 결과 (데이트 점수, 분위기 태그, 가격대, 추천 시간, 추천 이유)
     */
    @PostMapping("/curate")
    @Operation(summary = "장소 큐레이션", description = "Claude API를 활용하여 장소의 데이트 적합도를 분석합니다")
    public ResponseEntity<PlaceCurationResult> curatePlace(@RequestBody PlaceBasicInfo placeInfo) {
        log.info("Place curation request received for: {}", placeInfo.getPlaceName());

        try {
            PlaceCurationResult result = placeCurationService.analyzePlaceForDate(placeInfo);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to curate place: {}", placeInfo.getPlaceName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
