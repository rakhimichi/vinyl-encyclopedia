package com.kirill.vinylencyclopedia.naruto;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class NarutoCatalogTest {
    private final NarutoCatalog catalog = new NarutoCatalog();

    @Test
    void containsExactlyTheRequestedEpisodesWithNoDuplicatesOrFilledGaps() {
        assertEquals(592, catalog.total());
        assertEquals(178, catalog.items().stream().filter(i -> i.season() == 1).count());
        assertEquals(414, catalog.items().stream().filter(i -> i.season() == 2).count());
        assertEquals(expected("1-25,27-141,148-157,161,169-173,178-183,185,187-194,216-220"), episodes(1));
        assertEquals(expected("1-169,172-182,185,187-189,191,194,196-222,229,234,236,243-256,261-278,280,282-289,296-302,309-311,313-319,321-375,378-393,414-421,424-428,431,451-500"), episodes(2));
        assertEquals(592, catalog.items().stream().map(NarutoCatalog.Item::id).distinct().count());
    }

    @Test
    void preservesSpecialsAndTheirPlaceInTheViewingOrder() {
        assertEquals(Set.of("s1-ova-1", "s1-ova-2", "s2-ova-12", "s2-ova-13", "s2-movie-9", "s2-movie-10", "s2-movie-11"),
                catalog.items().stream().filter(i -> !i.kind().equals("EPISODE")).map(NarutoCatalog.Item::id).collect(Collectors.toSet()));
        List<String> ids = catalog.items().stream().map(NarutoCatalog.Item::id).toList();
        assertBetween(ids, "s1-ep-19", "s1-ova-1", "s1-ep-20");
        assertBetween(ids, "s1-ep-80", "s1-ova-2", "s1-ep-81");
        assertBetween(ids, "s2-ep-311", "s2-movie-9", "s2-ep-313");
        assertBetween(ids, "s2-ep-363", "s2-ova-12", "s2-ep-364");
        assertBetween(ids, "s2-ep-479", "s2-movie-10", "s2-ep-480");
        assertEquals(List.of("s2-ep-500", "s2-ova-13", "s2-movie-11"), ids.subList(ids.size() - 3, ids.size()));
        assertEquals("С+ФН", catalog.groups().stream().filter(g -> g.items().get(0).id().equals("s2-ep-451")).findFirst().orElseThrow().tags());
    }

    private Set<Integer> episodes(int season) {
        return catalog.items().stream().filter(i -> i.season() == season && i.kind().equals("EPISODE"))
                .map(NarutoCatalog.Item::number).collect(Collectors.toSet());
    }
    private Set<Integer> expected(String compact) {
        Set<Integer> numbers = new TreeSet<>();
        for (String range : compact.split(",")) {
            String[] bounds = range.split("-");
            int first = Integer.parseInt(bounds[0]);
            int last = Integer.parseInt(bounds[bounds.length - 1]);
            for (int n = first; n <= last; n++) numbers.add(n);
        }
        return numbers;
    }
    private void assertBetween(List<String> ids, String before, String special, String after) {
        int i = ids.indexOf(special);
        assertEquals(before, ids.get(i - 1)); assertEquals(after, ids.get(i + 1));
    }
}
