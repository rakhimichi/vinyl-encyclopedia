package com.kirill.vinylencyclopedia.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.AdminUserRecordsDto;
import com.kirill.vinylencyclopedia.dto.AdminUserSummaryDto;
import com.kirill.vinylencyclopedia.dto.ApiVinylRecordDto;
import com.kirill.vinylencyclopedia.service.AdminService;

@RestController
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/api/admin/users")
    public List<AdminUserSummaryDto> getAllUsers() {
        // This endpoint gives admin a compact JSON overview of users and their record statistics.
        return adminService.getAllUserSummaries();
    }

    @GetMapping("/api/admin/users/{userId}/records")
    public AdminUserRecordsDto getUserRecords(@PathVariable Long userId) {
        AppUser targetUser = adminService.getUserById(userId);

        List<ApiVinylRecordDto> myCollectionRecords = adminService.getMyCollectionRecordsForUser(userId).stream()
                .map(this::toDto)
                .toList();

        List<ApiVinylRecordDto> wishlistRecords = adminService.getWishlistRecordsForUser(userId).stream()
                .map(this::toDto)
                .toList();

        return new AdminUserRecordsDto(
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getFirstName() + " " + targetUser.getLastName(),
                myCollectionRecords,
                wishlistRecords
        );
    }

    @DeleteMapping("/api/admin/records/{recordId}")
    public Map<String, String> deleteRecordAsAdmin(@PathVariable Long recordId) {
        // The admin API returns a simple confirmation message instead of redirecting to an HTML page.
        adminService.deleteRecordAsAdmin(recordId);
        return Map.of("message", "Record deleted successfully");
    }

    private ApiVinylRecordDto toDto(VinylRecord record) {
        return new ApiVinylRecordDto(
                record.getId(),
                record.getArtist(),
                record.getTitle(),
                record.getGenre(),
                record.getReleaseYear(),
                record.getLabelName(),
                record.getCollectionSection() != null ? record.getCollectionSection().name() : null,
                record.getVinylFormat() != null ? record.getVinylFormat().name() : null,
                record.getNotes(),
                record.getCoverImagePath(),
                record.getOwner() != null ? record.getOwner().getUsername() : null
        );
    }
}
