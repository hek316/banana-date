package com.bananadate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배치 큐레이션 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCurationResult {

    /**
     * 큐레이션 성공한 장소 수
     */
    private int successCount;

    /**
     * 큐레이션 실패한 장소 수
     */
    private int failedCount;

    /**
     * 처리한 총 장소 수
     */
    private int totalProcessed;

    /**
     * 큐레이션에 소요된 시간 (초)
     */
    private long elapsedTimeSeconds;

    /**
     * 결과 메시지
     */
    private String message;
}
