package com.kirill.vinylencyclopedia.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.ApiVinylRecordDto;
import com.kirill.vinylencyclopedia.service.VinylRecordService;

@RestController
public class ApiRecordController {

    private final VinylRecordService vinylRecordService;

    public ApiRecordController(VinylRecordService vinylRecordService) {
        this.vinylRecordService = vinylRecordService;
    }

    @GetMapping("/api/records")
    public List<ApiVinylRecordDto> getRecords(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "sortBy", required = false, defaultValue = "artist") String sortBy,
            @RequestParam(name = "section", required = false) String section,
            Authentication authentication
    ) {
        String username = authentication.getName();

        // The API returns only the authenticated user's own records.
        return vinylRecordService.getFilteredRecordsForUser(username, query, sortBy).stream()
                .filter(record -> matchesSection(record, section))
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/api/records/{id}")
    public ApiVinylRecordDto getRecordById(@PathVariable Long id, Authentication authentication) {
        VinylRecord record = vinylRecordService.getRecordForUser(id, authentication.getName());
        return toDto(record);
    }

    private boolean matchesSection(VinylRecord record, String section) {
        if (section == null || section.isBlank()) {
            return true;
        }

        String normalizedSection = section.trim().toLowerCase();

        // API uses human-friendly section values so that the endpoint is easier to test manually.
        if (normalizedSection.equals("my-collection")) {
            return record.getCollectionSection() == CollectionSection.IN_MY_COLLECTION;
        }

        if (normalizedSection.equals("wishlist")) {
            return record.getCollectionSection() == CollectionSection.WISHLIST;
        }

        return true;
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
