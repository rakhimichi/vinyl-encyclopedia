package com.kirill.vinylencyclopedia.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.dto.ApiVinylRecordDto;
import com.kirill.vinylencyclopedia.service.VinylRecordService;

@ExtendWith(MockitoExtension.class)
class ApiRecordControllerTest {

    @Mock
    private VinylRecordService vinylRecordService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ApiRecordController apiRecordController;

    @Test
    void getRecords_returnsOnlyWishlistWhenSectionIsWishlist() {
        VinylRecord wishlistRecord = buildRecord("Justice", "Cross", CollectionSection.WISHLIST, "admin");
        VinylRecord myCollectionRecord = buildRecord("Daft Punk", "Discovery", CollectionSection.IN_MY_COLLECTION, "admin");

        when(authentication.getName()).thenReturn("admin");
        when(vinylRecordService.getFilteredRecordsForUser("admin", null, "artist"))
                .thenReturn(List.of(wishlistRecord, myCollectionRecord));

        List<ApiVinylRecordDto> result = apiRecordController.getRecords(null, "artist", "wishlist", authentication);

        assertEquals(1, result.size());
        assertEquals("Justice", result.get(0).getArtist());
        assertEquals("WISHLIST", result.get(0).getCollectionSection());
    }

    @Test
    void getRecordById_returnsMappedDto() {
        VinylRecord record = buildRecord("Daft Punk", "Discovery", CollectionSection.IN_MY_COLLECTION, "kirill");

        when(authentication.getName()).thenReturn("kirill");
        when(vinylRecordService.getRecordForUser(10L, "kirill")).thenReturn(record);

        ApiVinylRecordDto result = apiRecordController.getRecordById(10L, authentication);

        assertEquals("Daft Punk", result.getArtist());
        assertEquals("Discovery", result.getTitle());
        assertEquals("kirill", result.getOwnerUsername());
    }

    private VinylRecord buildRecord(
            String artist,
            String title,
            CollectionSection collectionSection,
            String ownerUsername
    ) {
        AppUser owner = new AppUser();
        owner.setUsername(ownerUsername);

        VinylRecord record = new VinylRecord();
        record.setArtist(artist);
        record.setTitle(title);
        record.setGenre("Electronic");
        record.setCollectionSection(collectionSection);
        record.setCoverImagePath("https://example.com/cover.jpg");
        record.setOwner(owner);

        return record;
    }
}
