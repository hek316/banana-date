package com.bananadate.service;

import com.bananadate.dto.BatchCurationResult;
import com.bananadate.dto.PlaceCurationResult;
import com.bananadate.entity.Place;
import com.bananadate.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PlaceBatchCurationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PlaceBatchCurationServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceCurationService placeCurationService;

    @InjectMocks
    private PlaceBatchCurationService placeBatchCurationService;

    private List<Place> mockUncuratedPlaces;
    private PlaceCurationResult mockCurationResult;

    @BeforeEach
    void setUp() {
        // Mock uncurated places
        mockUncuratedPlaces = Arrays.asList(
                createMockPlace(1L, "테스트 카페", "음식점 > 카페"),
                createMockPlace(2L, "테스트 레스토랑", "음식점 > 양식"),
                createMockPlace(3L, "테스트 술집", "음식점 > 주점")
        );

        // Mock curation result
        mockCurationResult = PlaceCurationResult.builder()
                .dateScore(8)
                .moodTags(Arrays.asList("#로맨틱", "#고급스러운", "#분위기좋은"))
                .priceRange("30,000-50,000원")
                .bestTime("저녁 6-9시")
                .recommendation("테스트 추천")
                .build();
    }

    @Test
    void testCurateUncuratedPlaces_Success() {
        // Given: 큐레이션되지 않은 장소 3개가 있고 모두 성공하는 경우
        when(placeRepository.findUncuratedPlaces()).thenReturn(mockUncuratedPlaces);
        when(placeCurationService.analyzePlaceForDate(any())).thenReturn(mockCurationResult);
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 배치 큐레이션 실행
        BatchCurationResult result = placeBatchCurationService.curateUncuratedPlaces(null);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getSuccessCount()).isEqualTo(3);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getTotalProcessed()).isEqualTo(3);
        assertThat(result.getElapsedTimeSeconds()).isGreaterThanOrEqualTo(0);

        // Claude API가 3번 호출되었는지 확인
        verify(placeCurationService, times(3)).analyzePlaceForDate(any());
        // DB에 3개 저장되었는지 확인
        verify(placeRepository, times(3)).save(any(Place.class));
    }

    @Test
    void testCurateUncuratedPlaces_WithLimit() {
        // Given: 큐레이션되지 않은 장소 3개가 있지만 limit=2로 제한
        when(placeRepository.findUncuratedPlaces()).thenReturn(mockUncuratedPlaces);
        when(placeCurationService.analyzePlaceForDate(any())).thenReturn(mockCurationResult);
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: limit=2로 배치 큐레이션 실행
        BatchCurationResult result = placeBatchCurationService.curateUncuratedPlaces(2);

        // Then: 2개만 처리되었는지 검증
        assertThat(result).isNotNull();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getTotalProcessed()).isEqualTo(2);

        // Claude API가 2번만 호출되었는지 확인
        verify(placeCurationService, times(2)).analyzePlaceForDate(any());
        verify(placeRepository, times(2)).save(any(Place.class));
    }

    @Test
    void testCurateUncuratedPlaces_PartialFailure() {
        // Given: 일부 장소의 큐레이션이 실패하는 경우
        when(placeRepository.findUncuratedPlaces()).thenReturn(mockUncuratedPlaces);
        when(placeCurationService.analyzePlaceForDate(any()))
                .thenReturn(mockCurationResult)  // 첫 번째 성공
                .thenThrow(new RuntimeException("API Error"))  // 두 번째 실패
                .thenReturn(mockCurationResult);  // 세 번째 성공
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 배치 큐레이션 실행
        BatchCurationResult result = placeBatchCurationService.curateUncuratedPlaces(null);

        // Then: 결과 검증 (2개 성공, 1개 실패)
        assertThat(result).isNotNull();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(1);
        assertThat(result.getTotalProcessed()).isEqualTo(3);

        // Claude API가 3번 호출되었는지 확인
        verify(placeCurationService, times(3)).analyzePlaceForDate(any());
        // DB에는 성공한 2개만 저장되었는지 확인
        verify(placeRepository, times(2)).save(any(Place.class));
    }

    @Test
    void testCurateSinglePlace_Success() {
        // Given: 특정 장소를 큐레이션하는 경우
        Place mockPlace = createMockPlace(1L, "테스트 카페", "음식점 > 카페");
        when(placeRepository.findById(1L)).thenReturn(Optional.of(mockPlace));
        when(placeCurationService.analyzePlaceForDate(any())).thenReturn(mockCurationResult);
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 단일 장소 큐레이션 실행
        PlaceCurationResult result = placeBatchCurationService.curateSinglePlace(1L);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getDateScore()).isEqualTo(8);
        assertThat(result.getMoodTags()).hasSize(3);
        assertThat(result.getPriceRange()).isEqualTo("30,000-50,000원");
        assertThat(result.getBestTime()).isEqualTo("저녁 6-9시");
        assertThat(result.getRecommendation()).isEqualTo("테스트 추천");

        // Place 엔티티가 업데이트되었는지 확인
        assertThat(mockPlace.getDateScore()).isEqualTo(8);
        assertThat(mockPlace.getMoodTags()).hasSize(3);
        assertThat(mockPlace.getCuratedAt()).isNotNull();

        verify(placeCurationService, times(1)).analyzePlaceForDate(any());
        verify(placeRepository, times(1)).save(mockPlace);
    }

    @Test
    void testCurateSinglePlace_PlaceNotFound() {
        // Given: 존재하지 않는 장소 ID
        when(placeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: IllegalArgumentException 발생해야 함
        assertThatThrownBy(() -> placeBatchCurationService.curateSinglePlace(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Place not found with ID: 999");

        verify(placeCurationService, never()).analyzePlaceForDate(any());
        verify(placeRepository, never()).save(any(Place.class));
    }

    /**
     * Mock Place 생성 헬퍼 메서드
     */
    private Place createMockPlace(Long id, String placeName, String category) {
        Place place = new Place();
        place.setId(id);
        place.setKakaoPlaceId(String.valueOf(id));
        place.setPlaceName(placeName);
        place.setCategory(category);
        place.setAddress("서울특별시 강남구");
        place.setLatitude(37.4979);
        place.setLongitude(127.0276);
        place.setPhone("02-1234-5678");
        place.setPlaceUrl("http://place.kakao.com/" + id);
        place.setCreatedAt(LocalDateTime.now());
        place.setUpdatedAt(LocalDateTime.now());
        return place;
    }
}
