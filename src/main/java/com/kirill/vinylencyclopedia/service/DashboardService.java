package com.kirill.vinylencyclopedia.service;

import org.springframework.stereotype.Service;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.dto.DashboardStatsDto;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@Service
public class DashboardService {

    private final VinylRecordRepository vinylRecordRepository;

    public DashboardService(VinylRecordRepository vinylRecordRepository) {
        this.vinylRecordRepository = vinylRecordRepository;
    }

    public DashboardStatsDto getStatsForUser(String username) {
        long totalRecords = vinylRecordRepository.countByOwnerUsername(username);
        long inMyCollectionRecords = vinylRecordRepository.countByOwnerUsernameAndCollectionSection(
                username, CollectionSection.IN_MY_COLLECTION
        );
        long wishlistRecords = vinylRecordRepository.countByOwnerUsernameAndCollectionSection(
                username, CollectionSection.WISHLIST
        );

        return new DashboardStatsDto(totalRecords, inMyCollectionRecords, wishlistRecords);
    }
}
