package com.kirill.vinylencyclopedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;

public interface VinylRecordRepository extends JpaRepository<VinylRecord, Long> {

    List<VinylRecord> findByOwnerUsernameOrderByCreatedAtDesc(String username);

    long countByOwnerUsername(String username);

    long countByOwnerUsernameAndCollectionSection(String username, CollectionSection collectionSection);
}
