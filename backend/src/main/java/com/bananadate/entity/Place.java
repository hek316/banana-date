package com.bananadate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 장소 엔티티
 * 카카오 Local API로 수집한 기본 정보와 Claude API로 분석한 큐레이션 정보를 저장
 */
@Entity
@Table(name = "places", indexes = {
        @Index(name = "idx_kakao_place_id", columnList = "kakao_place_id", unique = true),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_curated_at", columnList = "curated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== Kakao API 기본 정보 ==========

    /**
     * 카카오 장소 ID (고유 식별자)
     */
    @Column(name = "kakao_place_id", nullable = false, unique = true)
    private String kakaoPlaceId;

    /**
     * 장소 이름
     */
    @Column(name = "place_name", nullable = false)
    private String placeName;

    /**
     * 카테고리 (예: "음식점 > 카페 > 커피전문점")
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * 주소
     */
    @Column(name = "address", nullable = false)
    private String address;

    /**
     * 위도
     */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /**
     * 경도
     */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /**
     * 전화번호
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 카카오맵 URL
     */
    @Column(name = "place_url")
    private String placeUrl;

    // ========== Claude API 큐레이션 정보 (나중에 채움) ==========

    /**
     * 데이트 적합도 점수 (1-10)
     */
    @Column(name = "date_score")
    private Integer dateScore;

    /**
     * 분위기 태그 (JSON 배열로 저장)
     */
    @ElementCollection
    @CollectionTable(name = "place_mood_tags", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "mood_tag")
    private List<String> moodTags;

    /**
     * 예상 가격대
     */
    @Column(name = "price_range")
    private String priceRange;

    /**
     * 추천 시간대
     */
    @Column(name = "best_time")
    private String bestTime;

    /**
     * 한 줄 추천 이유
     */
    @Column(name = "recommendation")
    private String recommendation;

    // ========== 메타 정보 ==========

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 큐레이션 완료 시각
     */
    @Column(name = "curated_at")
    private LocalDateTime curatedAt;

    /**
     * 큐레이션 완료 여부
     */
    public boolean isCurated() {
        return dateScore != null && curatedAt != null;
    }
}
