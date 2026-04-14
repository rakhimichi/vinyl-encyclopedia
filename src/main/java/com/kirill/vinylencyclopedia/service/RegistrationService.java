package com.kirill.vinylencyclopedia.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.Role;
import com.kirill.vinylencyclopedia.dto.RegistrationFormDto;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.RoleRepository;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(RegistrationFormDto formDto) {
        validateRegistration(formDto);

        // Every newly registered account gets the USER role by default.
        // ADMIN accounts are not created through the public registration page.
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role is missing in the database."));

        AppUser user = new AppUser();
        user.setUsername(formDto.getUsername().trim());
        user.setEmail(formDto.getEmail().trim().toLowerCase());
        user.setFirstName(formDto.getFirstName().trim());
        user.setLastName(formDto.getLastName().trim());

        // Password is encoded before saving so the database never stores plain text credentials.
        user.setPassword(passwordEncoder.encode(formDto.getPassword()));
        user.addRole(userRole);

        appUserRepository.save(user);
    }

    private void validateRegistration(RegistrationFormDto formDto) {
        // Password confirmation is checked manually because it compares two different fields.
        if (!formDto.getPassword().equals(formDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        // Username and email must stay unique so that login and account ownership remain clear.
        if (appUserRepository.existsByUsername(formDto.getUsername().trim())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (appUserRepository.existsByEmail(formDto.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
    }
}
