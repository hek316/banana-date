package com.bananadate.repository;

import com.bananadate.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Place 엔티티의 데이터베이스 접근을 위한 Repository
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * 카카오 장소 ID로 장소 조회
     */
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);

    /**
     * 카카오 장소 ID 존재 여부 확인
     */
    boolean existsByKakaoPlaceId(String kakaoPlaceId);

    /**
     * 카테고리로 장소 목록 조회
     */
    List<Place> findByCategory(String category);

    /**
     * 카테고리에 특정 키워드가 포함된 장소 목록 조회
     */
    List<Place> findByCategoryContaining(String categoryKeyword);

    /**
     * 카테고리에 특정 키워드가 포함된 장소 목록 조회 (페이징)
     */
    Page<Place> findByCategoryContaining(String categoryKeyword, Pageable pageable);

    /**
     * 큐레이션되지 않은 장소 목록 조회
     */
    @Query("SELECT p FROM Place p WHERE p.dateScore IS NULL OR p.curatedAt IS NULL")
    List<Place> findUncuratedPlaces();

    /**
     * 큐레이션되지 않은 장소 목록 조회 (페이징)
     */
    @Query("SELECT p FROM Place p WHERE p.dateScore IS NULL OR p.curatedAt IS NULL")
    Page<Place> findUncuratedPlaces(Pageable pageable);

    /**
     * 큐레이션된 장소 목록 조회
     */
    @Query("SELECT p FROM Place p WHERE p.dateScore IS NOT NULL AND p.curatedAt IS NOT NULL")
    List<Place> findCuratedPlaces();

    /**
     * 큐레이션된 장소 목록 조회 (페이징)
     */
    @Query("SELECT p FROM Place p WHERE p.dateScore IS NOT NULL AND p.curatedAt IS NOT NULL")
    Page<Place> findCuratedPlaces(Pageable pageable);

    /**
     * 데이트 점수 범위로 장소 목록 조회
     */
    @Query("SELECT p FROM Place p WHERE p.dateScore BETWEEN :minScore AND :maxScore ORDER BY p.dateScore DESC")
    List<Place> findByDateScoreBetween(int minScore, int maxScore);

    /**
     * 전체 장소 수 조회
     */
    long count();

    /**
     * 큐레이션된 장소 수 조회
     */
    @Query("SELECT COUNT(p) FROM Place p WHERE p.dateScore IS NOT NULL AND p.curatedAt IS NOT NULL")
    long countCuratedPlaces();

    /**
     * 큐레이션되지 않은 장소 수 조회
     */
    @Query("SELECT COUNT(p) FROM Place p WHERE p.dateScore IS NULL OR p.curatedAt IS NULL")
    long countUncuratedPlaces();
}
