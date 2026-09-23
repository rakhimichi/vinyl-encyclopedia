package com.kirill.vinylencyclopedia.controller;

import com.kirill.vinylencyclopedia.naruto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/pet-projects/naruto/api")
public class NarutoTrackerController {
    public record WatchRequest(@NotNull Boolean watched, LocalDate watchedOn, Instant expectedMarkedAt) {}
    private final NarutoTrackerService service;
    private final NarutoCatalog catalog;
    public NarutoTrackerController(NarutoTrackerService service, NarutoCatalog catalog) {
        this.service = service;
        this.catalog = catalog;
    }

    private void requireAccess(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("narutoAccessGranted"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unlock Naruto first");
        }
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<NarutoCatalog.Group>> catalog(HttpServletRequest request) {
        requireAccess(request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(catalog.groups());
    }

    @GetMapping("/state")
    public ResponseEntity<NarutoTrackerService.State> state(HttpServletRequest request, Principal principal) {
        requireAccess(request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.state(principal.getName()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<NarutoTrackerService.State> setWatched(@PathVariable String id,
            @Valid @RequestBody WatchRequest update, HttpServletRequest request, Principal principal) {
        requireAccess(request);
        service.setWatched(id, update.watched(), update.watchedOn(), update.expectedMarkedAt(), principal.getName());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.state(principal.getName()));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, String>> concurrentChange() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Progress changed. Please refresh."));
    }
}
