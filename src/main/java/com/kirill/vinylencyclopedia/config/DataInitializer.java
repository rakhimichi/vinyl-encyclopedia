package com.kirill.vinylencyclopedia.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kirill.vinylencyclopedia.domain.AppUser;
import com.kirill.vinylencyclopedia.domain.Role;
import com.kirill.vinylencyclopedia.repository.AppUserRepository;
import com.kirill.vinylencyclopedia.repository.RoleRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(
            RoleRepository roleRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Roles are created only once. If they already exist, the existing database values are reused.
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER")));

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

            // Demo admin account is created only if it does not exist yet.
            // This makes the project easier to demonstrate after a fresh deployment.
            if (appUserRepository.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "admin@vinyl.local",
                        "Admin",
                        "User"
                );

                admin.addRole(adminRole);
                admin.addRole(userRole);
                appUserRepository.save(admin);
            }

            // Demo regular user account is also prepared for quick testing.
            if (appUserRepository.findByUsername("user").isEmpty()) {
                AppUser user = new AppUser(
                        "user",
                        passwordEncoder.encode("user123"),
                        "user@vinyl.local",
                        "Regular",
                        "User"
                );

                user.addRole(userRole);
                appUserRepository.save(user);
            }
        };
    }
}
