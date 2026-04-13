package com.kirill.vinylencyclopedia.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.VinylRecordFormDto;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@Service
public class VinylRecordService {

    private final VinylRecordRepository vinylRecordRepository;
    private final AppUserRepository appUserRepository;

    public VinylRecordService(VinylRecordRepository vinylRecordRepository, AppUserRepository appUserRepository) {
        this.vinylRecordRepository = vinylRecordRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<VinylRecord> getRecordsForUser(String username) {
        return sortRecords(
                vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc(username),
                "artist"
        );
    }

    public List<VinylRecord> getInMyCollectionRecordsForUser(String username) {
        return sortRecords(
                vinylRecordRepository.findByOwnerUsernameAndCollectionSectionOrderByCreatedAtDesc(
                        username,
                        CollectionSection.IN_MY_COLLECTION
                ),
                "artist"
        );
    }

    public List<VinylRecord> getWishlistRecordsForUser(String username) {
        return sortRecords(
                vinylRecordRepository.findByOwnerUsernameAndCollectionSectionOrderByCreatedAtDesc(
                        username,
                        CollectionSection.WISHLIST
                ),
                "artist"
        );
    }

    public List<VinylRecord> getFilteredRecordsForUser(String username, String query, String sortBy) {
        List<VinylRecord> filteredRecords = vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc(username).stream()
                .filter(record -> matchesQuery(record, query))
                .toList();

        return sortRecords(filteredRecords, sortBy);
    }

    public List<VinylRecord> getFilteredInMyCollectionRecordsForUser(String username, String query, String sortBy) {
        return getFilteredRecordsForUser(username, query, sortBy).stream()
                .filter(record -> record.getCollectionSection() == CollectionSection.IN_MY_COLLECTION)
                .toList();
    }

    public List<VinylRecord> getFilteredWishlistRecordsForUser(String username, String query, String sortBy) {
        return getFilteredRecordsForUser(username, query, sortBy).stream()
                .filter(record -> record.getCollectionSection() == CollectionSection.WISHLIST)
                .toList();
    }

    public VinylRecord getRecordForUser(Long id, String username) {
        return vinylRecordRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
    }

    public void createRecord(VinylRecordFormDto formDto, String username) {
        AppUser owner = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        VinylRecord record = new VinylRecord();
        record.setArtist(formDto.getArtist());
        record.setTitle(formDto.getTitle());
        record.setGenre(formDto.getGenre());
        record.setReleaseYear(formDto.getReleaseYear());
        record.setLabelName(formDto.getLabelName());
        record.setCollectionSection(formDto.getCollectionSection());
        record.setVinylFormat(formDto.getVinylFormat());
        record.setNotes(formDto.getNotes());
        record.setCoverImagePath(formDto.getCoverImagePath());
        record.setOwner(owner);

        vinylRecordRepository.save(record);
    }

    public void updateRecord(Long id, VinylRecordFormDto formDto, String username) {
        VinylRecord record = getRecordForUser(id, username);

        record.setArtist(formDto.getArtist());
        record.setTitle(formDto.getTitle());
        record.setGenre(formDto.getGenre());
        record.setReleaseYear(formDto.getReleaseYear());
        record.setLabelName(formDto.getLabelName());
        record.setCollectionSection(formDto.getCollectionSection());
        record.setVinylFormat(formDto.getVinylFormat());
        record.setNotes(formDto.getNotes());
        record.setCoverImagePath(formDto.getCoverImagePath());

        vinylRecordRepository.save(record);
    }

    public void deleteRecord(Long id, String username) {
        VinylRecord record = getRecordForUser(id, username);
        vinylRecordRepository.delete(record);
    }

    private List<VinylRecord> sortRecords(List<VinylRecord> records, String sortBy) {
        String normalizedSortBy = normalizeSortBy(sortBy);

        Comparator<VinylRecord> comparator;

        if ("title".equals(normalizedSortBy)) {
            comparator = Comparator
                    .comparing((VinylRecord record) -> normalize(record.getTitle()))
                    .thenComparing(record -> normalize(record.getArtist()));
        } else {
            comparator = Comparator
                    .comparing((VinylRecord record) -> normalize(record.getArtist()))
                    .thenComparing(record -> normalize(record.getTitle()));
        }

        return records.stream()
                .sorted(comparator)
                .toList();
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "artist";
        }

        String normalized = sortBy.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("title") ? "title" : "artist";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesQuery(VinylRecord record, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        return containsIgnoreCase(record.getArtist(), normalizedQuery)
                || containsIgnoreCase(record.getTitle(), normalizedQuery)
                || containsIgnoreCase(record.getGenre(), normalizedQuery)
                || containsIgnoreCase(record.getLabelName(), normalizedQuery);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}