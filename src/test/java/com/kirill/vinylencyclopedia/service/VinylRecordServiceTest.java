package com.kirill.vinylencyclopedia.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@ExtendWith(MockitoExtension.class)
class VinylRecordServiceTest {

    @Mock
    private VinylRecordRepository vinylRecordRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private VinylRecordService vinylRecordService;

    @Test
    void getFilteredRecordsForUser_sortsByArtistAndThenTitle() {
        VinylRecord record1 = buildRecord("Coldplay", "Parachutes", "Alternative", "Parlophone", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record2 = buildRecord("ABBA", "Gold", "Pop", "Polar", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record3 = buildRecord("ABBA", "Arrival", "Pop", "Polar", CollectionSection.WISHLIST);

        when(vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc("kirill"))
                .thenReturn(List.of(record1, record2, record3));

        List<VinylRecord> result = vinylRecordService.getFilteredRecordsForUser("kirill", null, "artist");

        assertEquals(3, result.size());
        assertEquals("ABBA", result.get(0).getArtist());
        assertEquals("Arrival", result.get(0).getTitle());
        assertEquals("ABBA", result.get(1).getArtist());
        assertEquals("Gold", result.get(1).getTitle());
        assertEquals("Coldplay", result.get(2).getArtist());
        assertEquals("Parachutes", result.get(2).getTitle());
    }

    @Test
    void getFilteredRecordsForUser_sortsByTitleWhenRequested() {
        VinylRecord record1 = buildRecord("Daft Punk", "Discovery", "Electronic", "Virgin", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record2 = buildRecord("Coldplay", "A Rush of Blood to the Head", "Alternative", "Parlophone", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record3 = buildRecord("ABBA", "Gold", "Pop", "Polar", CollectionSection.WISHLIST);

        when(vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc("kirill"))
                .thenReturn(List.of(record1, record2, record3));

        List<VinylRecord> result = vinylRecordService.getFilteredRecordsForUser("kirill", null, "title");

        assertEquals(3, result.size());
        assertEquals("A Rush of Blood to the Head", result.get(0).getTitle());
        assertEquals("Discovery", result.get(1).getTitle());
        assertEquals("Gold", result.get(2).getTitle());
    }

    @Test
    void getFilteredRecordsForUser_filtersBySearchQuery() {
        VinylRecord record1 = buildRecord("Daft Punk", "Discovery", "Electronic", "Virgin", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record2 = buildRecord("Tame Impala", "Currents", "Psychedelic Rock", "Interscope", CollectionSection.IN_MY_COLLECTION);
        VinylRecord record3 = buildRecord("Justice", "Cross", "Electronic", "Ed Banger", CollectionSection.WISHLIST);

        when(vinylRecordRepository.findByOwnerUsernameOrderByCreatedAtDesc("kirill"))
                .thenReturn(List.of(record1, record2, record3));

        List<VinylRecord> result = vinylRecordService.getFilteredRecordsForUser("kirill", "electronic", "artist");

        assertEquals(2, result.size());
        assertEquals("Daft Punk", result.get(0).getArtist());
        assertEquals("Justice", result.get(1).getArtist());
    }

    private VinylRecord buildRecord(
            String artist,
            String title,
            String genre,
            String labelName,
            CollectionSection collectionSection
    ) {
        VinylRecord record = new VinylRecord();
        record.setArtist(artist);
        record.setTitle(title);
        record.setGenre(genre);
        record.setLabelName(labelName);
        record.setCollectionSection(collectionSection);
        record.setCoverImagePath("https://example.com/cover.jpg");
        return record;
    }
}
