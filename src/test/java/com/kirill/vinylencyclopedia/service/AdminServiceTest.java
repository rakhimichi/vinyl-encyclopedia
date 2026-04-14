package com.kirill.vinylencyclopedia.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.VinylRecord;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.VinylRecordRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VinylRecordRepository vinylRecordRepository;

    @Mock
    private VinylRecordService vinylRecordService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getMyCollectionRecordsForUser_usesTargetUsername() {
        AppUser user = new AppUser();
        user.setUsername("kirill");

        VinylRecord record = new VinylRecord();
        record.setArtist("Daft Punk");
        record.setTitle("Discovery");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vinylRecordService.getInMyCollectionRecordsForUser("kirill"))
                .thenReturn(List.of(record));

        List<VinylRecord> result = adminService.getMyCollectionRecordsForUser(1L);

        assertEquals(1, result.size());
        assertEquals("Daft Punk", result.get(0).getArtist());
        verify(vinylRecordService).getInMyCollectionRecordsForUser("kirill");
    }

    @Test
    void deleteRecordAsAdmin_deletesFoundRecord() {
        VinylRecord record = new VinylRecord();

        when(vinylRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        adminService.deleteRecordAsAdmin(5L);

        verify(vinylRecordRepository).delete(record);
    }
}
