package com.kirill.vinylencyclopedia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.dto.AdminUserSummaryDto;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@Service
public class AdminService {

    private final AppUserRepository appUserRepository;
    private final VinylRecordRepository vinylRecordRepository;

    public AdminService(AppUserRepository appUserRepository, VinylRecordRepository vinylRecordRepository) {
        this.appUserRepository = appUserRepository;
        this.vinylRecordRepository = vinylRecordRepository;
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

        // Roles are converted to one compact string because the admin page is meant to be a quick overview.
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
