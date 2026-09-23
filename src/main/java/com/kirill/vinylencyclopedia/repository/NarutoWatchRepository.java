package com.kirill.vinylencyclopedia.repository;

import com.kirill.vinylencyclopedia.domain.NarutoWatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface NarutoWatchRepository extends JpaRepository<NarutoWatch, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select watch from NarutoWatch watch where watch.itemId = :id")
    Optional<NarutoWatch> findForUpdate(@Param("id") String id);
}
