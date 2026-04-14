package com.kirill.vinylencyclopedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // The REST API is added as an extra feature for the project.
                // To keep testing simpler in tools like Postman, CSRF protection is disabled only for /api/** routes.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // Public pages and static resources must stay available to anonymous users.
                        .requestMatchers(
                                "/", "/home", "/login", "/register",
                                "/css/**", "/images/**",
                                "/favicon.ico", "/vinyl-favicon.ico", "/**/*.ico"
                        ).permitAll()

                        // Admin web pages and admin API endpoints are limited to users with the ADMIN role.
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // The remaining API endpoints are available only to authenticated users.
                        .requestMatchers("/api/**").authenticated()

                        // Every other route in the application requires login.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is used because passwords must never be stored as plain text.
        return new BCryptPasswordEncoder();
    }
}
