package com.kirill.vinylencyclopedia.dto;

public class DashboardStatsDto {

    private final long totalRecords;
    private final long inMyCollectionRecords;
    private final long wishlistRecords;

    public DashboardStatsDto(long totalRecords, long inMyCollectionRecords, long wishlistRecords) {
        this.totalRecords = totalRecords;
        this.inMyCollectionRecords = inMyCollectionRecords;
        this.wishlistRecords = wishlistRecords;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getInMyCollectionRecords() {
        return inMyCollectionRecords;
    }

    public long getWishlistRecords() {
        return wishlistRecords;
    }
}
