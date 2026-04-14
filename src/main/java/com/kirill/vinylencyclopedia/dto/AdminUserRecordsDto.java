package com.kirill.vinylencyclopedia.dto;

import java.util.List;

public class AdminUserRecordsDto {

    private Long userId;
    private String username;
    private String fullName;
    private List<ApiVinylRecordDto> myCollectionRecords;
    private List<ApiVinylRecordDto> wishlistRecords;

    public AdminUserRecordsDto() {
    }

    public AdminUserRecordsDto(
            Long userId,
            String username,
            String fullName,
            List<ApiVinylRecordDto> myCollectionRecords,
            List<ApiVinylRecordDto> wishlistRecords
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.myCollectionRecords = myCollectionRecords;
        this.wishlistRecords = wishlistRecords;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public List<ApiVinylRecordDto> getMyCollectionRecords() {
        return myCollectionRecords;
    }

    public List<ApiVinylRecordDto> getWishlistRecords() {
        return wishlistRecords;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMyCollectionRecords(List<ApiVinylRecordDto> myCollectionRecords) {
        this.myCollectionRecords = myCollectionRecords;
    }

    public void setWishlistRecords(List<ApiVinylRecordDto> wishlistRecords) {
        this.wishlistRecords = wishlistRecords;
    }
}
