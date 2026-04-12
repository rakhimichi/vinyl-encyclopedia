package com.kirill.vinylencyclopedia.service;

import java.util.List;

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
        return vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc(username);
    }

    public List<VinylRecord> getInMyCollectionRecordsForUser(String username) {
        return vinylRecordRepository.findByOwnerUsernameAndCollectionSectionOrderByCreatedAtDesc(
                username,
                CollectionSection.IN_MY_COLLECTION
        );
    }

    public List<VinylRecord> getWishlistRecordsForUser(String username) {
        return vinylRecordRepository.findByOwnerUsernameAndCollectionSectionOrderByCreatedAtDesc(
                username,
                CollectionSection.WISHLIST
        );
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
}