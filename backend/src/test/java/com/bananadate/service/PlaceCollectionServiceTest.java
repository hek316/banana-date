package com.bananadate.service;

import com.bananadate.dto.KakaoLocalSearchResponse;
import com.bananadate.dto.PlaceCollectionResult;
import com.bananadate.entity.Place;
import com.bananadate.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PlaceCollectionService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PlaceCollectionServiceTest {

    @Mock
    private KakaoLocalApiService kakaoLocalApiService;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceCollectionService placeCollectionService;

    private List<KakaoLocalSearchResponse.Document> mockDocuments;

    @BeforeEach
    void setUp() {
        // Mock Kakao API 응답 데이터 생성
        mockDocuments = createMockDocuments(3);
    }

    @Test
    void testCollectPlaces_Success() {
        // Given: 카카오 API가 정상적으로 응답하고 중복이 없는 경우
        when(kakaoLocalApiService.searchPlacesWithLimit(anyString()))
                .thenReturn(mockDocuments);
        when(placeRepository.existsByKakaoPlaceId(anyString()))
                .thenReturn(false);
        when(placeRepository.save(any(Place.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: 장소 수집 실행
        PlaceCollectionResult result = placeCollectionService.collectPlaces();

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getCollectedCount()).isEqualTo(60); // 5 locations × 4 categories × 3 documents
        assertThat(result.getSkippedCount()).isEqualTo(0);
        assertThat(result.getTotalAttempted()).isEqualTo(60);

        // 카카오 API가 20번 호출되었는지 확인
        verify(kakaoLocalApiService, times(20)).searchPlacesWithLimit(anyString());
        // DB에 60개 저장되었는지 확인
        verify(placeRepository, times(60)).save(any(Place.class));
    }

    @Test
    void testCollectPlaces_WithDuplicates() {
        // Given: 일부 장소가 이미 DB에 존재하는 경우
        when(kakaoLocalApiService.searchPlacesWithLimit(anyString()))
                .thenReturn(mockDocuments);

        // 첫 번째 장소만 항상 중복, 나머지는 새로운 것
        when(placeRepository.existsByKakaoPlaceId("1"))
                .thenReturn(true);
        when(placeRepository.existsByKakaoPlaceId("2"))
                .thenReturn(false);
        when(placeRepository.existsByKakaoPlaceId("3"))
                .thenReturn(false);

        when(placeRepository.save(any(Place.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: 장소 수집 실행
        PlaceCollectionResult result = placeCollectionService.collectPlaces();

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getCollectedCount()).isEqualTo(40); // 20 queries × 2 new documents per query
        assertThat(result.getSkippedCount()).isEqualTo(20);   // 20 queries × 1 duplicate per query
        assertThat(result.getTotalAttempted()).isEqualTo(60);

        // DB에 40개만 저장되었는지 확인
        verify(placeRepository, times(40)).save(any(Place.class));
    }

    @Test
    void testCollectPlaces_EmptyResponse() {
        // Given: 카카오 API가 빈 결과를 반환하는 경우
        when(kakaoLocalApiService.searchPlacesWithLimit(anyString()))
                .thenReturn(Collections.emptyList());

        // When: 장소 수집 실행
        PlaceCollectionResult result = placeCollectionService.collectPlaces();

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getCollectedCount()).isEqualTo(0);
        assertThat(result.getSkippedCount()).isEqualTo(0);
        assertThat(result.getTotalAttempted()).isEqualTo(0);

        // 카카오 API는 20번 호출되었지만 저장은 0번
        verify(kakaoLocalApiService, times(20)).searchPlacesWithLimit(anyString());
        verify(placeRepository, never()).save(any(Place.class));
    }

    @Test
    void testCollectPlaces_PartialFailure() {
        // Given: 일부 쿼리가 실패하는 경우
        when(kakaoLocalApiService.searchPlacesWithLimit(anyString()))
                .thenReturn(mockDocuments)  // 첫 번째 쿼리는 성공
                .thenThrow(new RuntimeException("API Error"))  // 두 번째 쿼리는 실패
                .thenReturn(mockDocuments); // 세 번째 쿼리는 성공
        when(placeRepository.existsByKakaoPlaceId(anyString()))
                .thenReturn(false);
        when(placeRepository.save(any(Place.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: 장소 수집 실행
        PlaceCollectionResult result = placeCollectionService.collectPlaces();

        // Then: 결과 검증 (에러 발생해도 계속 진행)
        assertThat(result).isNotNull();
        assertThat(result.getCollectedCount()).isGreaterThan(0);
        assertThat(result.getTotalAttempted()).isGreaterThan(0);
    }

    /**
     * Mock 문서 생성 헬퍼 메서드
     */
    private List<KakaoLocalSearchResponse.Document> createMockDocuments(int count) {
        return Arrays.asList(
                createMockDocument("1", "강남 이탈리안 레스토랑", "음식점 > 이탈리안"),
                createMockDocument("2", "강남 카페", "음식점 > 카페"),
                createMockDocument("3", "강남 일식당", "음식점 > 일식")
        ).subList(0, Math.min(count, 3));
    }

    /**
     * Mock 문서 생성 헬퍼 메서드
     */
    private KakaoLocalSearchResponse.Document createMockDocument(String id, String placeName, String category) {
        KakaoLocalSearchResponse.Document doc = new KakaoLocalSearchResponse.Document();
        doc.setId(id);
        doc.setPlaceName(placeName);
        doc.setCategoryName(category);
        doc.setAddressName("서울특별시 강남구");
        doc.setRoadAddressName("서울특별시 강남구 테헤란로");
        doc.setPhone("02-1234-5678");
        doc.setX("127.0276");
        doc.setY("37.4979");
        doc.setPlaceUrl("http://place.kakao.com/" + id);
        return doc;
    }
}
