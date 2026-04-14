package com.kirill.vinylencyclopedia.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.Role;
import com.kirill.vinylencyclopedia.dto.RegistrationFormDto;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerNewUser_savesEncodedUser() {
        RegistrationFormDto formDto = new RegistrationFormDto();
        formDto.setUsername("newuser");
        formDto.setEmail("newuser@example.com");
        formDto.setFirstName("New");
        formDto.setLastName("User");
        formDto.setPassword("secret123");
        formDto.setConfirmPassword("secret123");

        Role userRole = new Role("USER");

        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        registrationService.registerNewUser(formDto);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals("encoded-password", savedUser.getPassword());
    }

    @Test
    void registerNewUser_throwsWhenPasswordsDoNotMatch() {
        RegistrationFormDto formDto = new RegistrationFormDto();
        formDto.setUsername("newuser");
        formDto.setEmail("newuser@example.com");
        formDto.setFirstName("New");
        formDto.setLastName("User");
        formDto.setPassword("secret123");
        formDto.setConfirmPassword("different123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerNewUser(formDto)
        );

        assertEquals("Passwords do not match.", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void registerNewUser_throwsWhenUsernameAlreadyExists() {
        RegistrationFormDto formDto = new RegistrationFormDto();
        formDto.setUsername("takenuser");
        formDto.setEmail("taken@example.com");
        formDto.setFirstName("Taken");
        formDto.setLastName("User");
        formDto.setPassword("secret123");
        formDto.setConfirmPassword("secret123");

        when(appUserRepository.existsByUsername("takenuser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerNewUser(formDto)
        );

        assertEquals("Username is already taken.", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}
