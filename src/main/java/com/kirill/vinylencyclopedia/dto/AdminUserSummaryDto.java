package com.kirill.vinylencyclopedia.dto;

public class AdminUserSummaryDto {

    private final Long userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final String roles;
    private final long totalRecords;
    private final long myCollectionRecords;
    private final long wishlistRecords;

    public AdminUserSummaryDto(
            Long userId,
            String username,
            String email,
            String fullName,
            String roles,
            long totalRecords,
            long myCollectionRecords,
            long wishlistRecords
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.totalRecords = totalRecords;
        this.myCollectionRecords = myCollectionRecords;
        this.wishlistRecords = wishlistRecords;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoles() {
        return roles;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getMyCollectionRecords() {
        return myCollectionRecords;
    }

    public long getWishlistRecords() {
        return wishlistRecords;
    }
}
