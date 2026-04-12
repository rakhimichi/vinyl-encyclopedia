package com.kirill.vinylencyclopedia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kirill.vinylencyclopedia.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
