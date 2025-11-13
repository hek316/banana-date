package com.bananadate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 카카오 Local API 키워드 검색 응답 DTO
 */
@Data
public class KakaoLocalSearchResponse {

    /**
     * 검색 결과 메타 정보
     */
    @JsonProperty("meta")
    private Meta meta;

    /**
     * 검색 결과 문서 목록
     */
    @JsonProperty("documents")
    private List<Document> documents;

    @Data
    public static class Meta {
        /**
         * 검색된 문서 수
         */
        @JsonProperty("total_count")
        private Integer totalCount;

        /**
         * 현재 페이지의 문서 수
         */
        @JsonProperty("pageable_count")
        private Integer pageableCount;

        /**
         * 마지막 페이지 여부
         */
        @JsonProperty("is_end")
        private Boolean isEnd;
    }

    @Data
    public static class Document {
        /**
         * 장소 ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * 장소명, 업체명
         */
        @JsonProperty("place_name")
        private String placeName;

        /**
         * 카테고리 이름
         */
        @JsonProperty("category_name")
        private String categoryName;

        /**
         * 카테고리 그룹 코드
         */
        @JsonProperty("category_group_code")
        private String categoryGroupCode;

        /**
         * 카테고리 그룹 이름
         */
        @JsonProperty("category_group_name")
        private String categoryGroupName;

        /**
         * 전화번호
         */
        @JsonProperty("phone")
        private String phone;

        /**
         * 전체 지번 주소
         */
        @JsonProperty("address_name")
        private String addressName;

        /**
         * 전체 도로명 주소
         */
        @JsonProperty("road_address_name")
        private String roadAddressName;

        /**
         * X 좌표값, 경도(longitude)
         */
        @JsonProperty("x")
        private String x;

        /**
         * Y 좌표값, 위도(latitude)
         */
        @JsonProperty("y")
        private String y;

        /**
         * 장소 상세페이지 URL
         */
        @JsonProperty("place_url")
        private String placeUrl;

        /**
         * 중심좌표까지의 거리 (meter 단위)
         */
        @JsonProperty("distance")
        private String distance;
    }
}
