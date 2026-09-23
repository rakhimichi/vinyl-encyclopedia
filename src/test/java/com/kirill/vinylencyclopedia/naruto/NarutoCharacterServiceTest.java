package com.kirill.vinylencyclopedia.naruto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class NarutoCharacterServiceTest {
    @Test
    void dailyChoiceIsStableAcrossReloadsAndHasFullCatalog() {
        var service = new NarutoCharacterService();
        var today = LocalDate.of(2026, 9, 24);
        var character = service.forDay("kirill", today);
        assertEquals(145, character.poolSize());
        assertEquals(character, new NarutoCharacterService().forDay("kirill", today));
        assertTrue(character.url().startsWith("https://naruto.fandohub.com/ru/character/"));
        assertTrue(IntStream.range(0, 20).mapToObj(n -> service.forDay("friend-" + n, today).name()).distinct().count() > 10);
        assertTrue(IntStream.range(0, 20).mapToObj(n -> service.forDay("kirill", today.plusDays(n)).name()).distinct().count() > 10);
    }
}
