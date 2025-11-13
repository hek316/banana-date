package com.bananadate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 수집 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCollectionResult {

    /**
     * 수집된 장소 수
     */
    private int collectedCount;

    /**
     * 중복으로 건너뛴 장소 수
     */
    private int skippedCount;

    /**
     * 총 시도한 수집 수
     */
    private int totalAttempted;

    /**
     * 수집에 소요된 시간 (초)
     */
    private long elapsedTimeSeconds;

    /**
     * 수집 상태 메시지
     */
    private String message;
}
