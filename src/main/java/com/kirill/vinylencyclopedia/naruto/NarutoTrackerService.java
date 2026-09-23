package com.kirill.vinylencyclopedia.naruto;

import com.kirill.vinylencyclopedia.domain.NarutoWatch;
import com.kirill.vinylencyclopedia.repository.NarutoWatchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service
public class NarutoTrackerService {
    public record Mark(LocalDate watchedOn, Instant markedAt, String markedBy) {}
    public record State(NarutoStats.Summary stats, Map<String, Mark> marks,
                        NarutoCharacterService.DailyCharacter character, String username) {}
    private final NarutoWatchRepository repository;
    private final NarutoCatalog catalog;
    private final NarutoCharacterService characters;
    private final Clock clock;

    public NarutoTrackerService(NarutoWatchRepository repository, NarutoCatalog catalog,
                                NarutoCharacterService characters, Clock clock) {
        this.repository = repository;
        this.catalog = catalog;
        this.characters = characters;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public State state(String username) {
        LocalDate today = LocalDate.now(clock.withZone(NarutoStats.ZONE));
        List<NarutoWatch> watches = repository.findAll().stream()
                .filter(w -> catalog.contains(w.getItemId())).toList();
        Map<String, Mark> marks = new LinkedHashMap<>();
        watches.forEach(w -> marks.put(w.getItemId(), new Mark(w.getWatchedOn(), w.getMarkedAt(), w.getMarkedBy())));
        return new State(NarutoStats.calculate(catalog.total(), watches, today), marks,
                characters.forDay(username, today), username);
    }

    @Transactional
    public void setWatched(String itemId, boolean watched, LocalDate watchedOn, Instant expectedMarkedAt, String username) {
        if (!catalog.contains(itemId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown episode");
        LocalDate today = LocalDate.now(clock.withZone(NarutoStats.ZONE));
        if (watched && (watchedOn == null || watchedOn.isAfter(today) || watchedOn.isBefore(LocalDate.of(2002, 1, 1)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a viewing date up to today");
        }
        Optional<NarutoWatch> existing = repository.findForUpdate(itemId);
        if (watched && existing.isEmpty()) {
            // The primary key prevents two friends counting the same episode twice.
            repository.saveAndFlush(new NarutoWatch(itemId, watchedOn, clock.instant(), username));
        } else if (!watched && existing.isPresent()) {
            if (!Objects.equals(existing.get().getMarkedAt(), expectedMarkedAt)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Progress changed; refresh before removing this mark");
            }
            repository.delete(existing.get());
            repository.flush();
        }
    }
}
