package com.kirill.vinylencyclopedia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.AdminUserSummaryDto;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@Service
public class AdminService {

    private final AppUserRepository appUserRepository;
    private final VinylRecordRepository vinylRecordRepository;
    private final VinylRecordService vinylRecordService;

    public AdminService(
            AppUserRepository appUserRepository,
            VinylRecordRepository vinylRecordRepository,
            VinylRecordService vinylRecordService
    ) {
        this.appUserRepository = appUserRepository;
        this.vinylRecordRepository = vinylRecordRepository;
        this.vinylRecordService = vinylRecordService;
    }

    public List<AdminUserSummaryDto> getAllUserSummaries() {
        return appUserRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    public long getTotalUsers() {
        return appUserRepository.count();
    }

    public long getTotalRecords() {
        return vinylRecordRepository.count();
    }

    public AppUser getUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public List<VinylRecord> getMyCollectionRecordsForUser(Long userId) {
        AppUser user = getUserById(userId);
        return vinylRecordService.getInMyCollectionRecordsForUser(user.getUsername());
    }

    public List<VinylRecord> getWishlistRecordsForUser(Long userId) {
        AppUser user = getUserById(userId);
        return vinylRecordService.getWishlistRecordsForUser(user.getUsername());
    }

    public void deleteRecordAsAdmin(Long recordId) {
        // This method is used only from /admin/** and /api/admin/** routes.
        // Because those routes are protected by ROLE_ADMIN, the normal owner check is bypassed here on purpose.
        VinylRecord record = vinylRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        vinylRecordRepository.delete(record);
    }

    private AdminUserSummaryDto toSummary(AppUser user) {
        long totalRecords = vinylRecordRepository.countByOwnerUsername(user.getUsername());
        long myCollectionRecords = vinylRecordRepository.countByOwnerUsernameAndCollectionSection(
                user.getUsername(),
                CollectionSection.IN_MY_COLLECTION
        );
        long wishlistRecords = vinylRecordRepository.countByOwnerUsernameAndCollectionSection(
                user.getUsername(),
                CollectionSection.WISHLIST
        );

        // Roles are collected into one short string because the admin page is designed as a quick overview table.
        String roles = user.getRoles().stream()
                .map(role -> role.getName())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("USER");

        return new AdminUserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                roles,
                totalRecords,
                myCollectionRecords,
                wishlistRecords
        );
    }
}