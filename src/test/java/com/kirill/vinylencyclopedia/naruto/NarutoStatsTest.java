package com.kirill.vinylencyclopedia.naruto;

import com.kirill.vinylencyclopedia.domain.NarutoWatch;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class NarutoStatsTest {
    private final LocalDate start = NarutoStats.START;
    @Test
    void startsWithThreePerDayAndCorrectCalendarDeadline() {
        var s = NarutoStats.calculate(592, List.of(), start.minusDays(1));
        assertEquals(3, s.pace());
        assertEquals(LocalDate.of(2027, 4, 9), s.predictedFinish());
        assertEquals(LocalDate.of(2027, 9, 24), s.deadline());
        assertEquals("NOT_STARTED", s.status());
        assertEquals(0, s.percent());
        assertEquals(3, NarutoStats.calculate(592, List.of(), start).pace());
    }
    @Test
    void fasterAndSlowerViewingMoveTheForecastInBothDirections() {
        LocalDate today = start.plusDays(6);
        var fast = NarutoStats.calculate(592, spread(6, 7), today);
        var slow = NarutoStats.calculate(592, spread(1, 7), today);
        assertEquals(6, fast.pace()); assertEquals(1, slow.pace());
        assertTrue(fast.predictedFinish().isBefore(fast.baselineFinish()));
        assertTrue(slow.predictedFinish().isAfter(slow.baselineFinish()));
        assertEquals("ON_TRACK", fast.status()); assertEquals("AT_RISK", slow.status());
    }
    @Test
    void zeroDaysCountAndOldViewsAgeOutOfRollingWindow() {
        var watches = spread(3, 1);
        var s = NarutoStats.calculate(592, watches, start.plusDays(6));
        assertEquals(3.0 / 7, s.pace());
        var paused = NarutoStats.calculate(592, watches, start.plusDays(7));
        assertEquals(0, paused.pace()); assertNull(paused.predictedFinish());
        assertEquals("PAUSED", paused.status());
        assertEquals(3, paused.watched());
        assertEquals(0, paused.activity().get(29).count());
    }
    @Test
    void removingAMarkRecalculatesPercentAndHistory() {
        List<NarutoWatch> watches = new ArrayList<>(spread(3, 1));
        assertEquals(50, NarutoStats.calculate(6, watches, start).percent());
        watches.remove(0);
        var s = NarutoStats.calculate(6, watches, start);
        assertEquals(100.0 / 3, s.percent(), .00001);
        assertEquals(2, s.todayCount()); assertEquals(4, s.remaining());
    }
    @Test
    void deadlineIncludesItsFinalDayAndLateCompletionStillLoses() {
        LocalDate deadline = NarutoStats.DEADLINE;
        assertNotEquals("LOST", NarutoStats.calculate(1, List.of(), deadline).status());
        assertEquals("LOST", NarutoStats.calculate(1, List.of(), deadline.plusDays(1)).status());
        assertEquals("WON", NarutoStats.calculate(1, List.of(watch("one", deadline)), deadline.plusDays(30)).status());
        assertEquals("LOST", NarutoStats.calculate(1, List.of(watch("one", deadline.plusDays(1))), deadline.plusDays(1)).status());
    }
    @Test
    void viewsBeforeStartCountForProgressButNotForMissionPace() {
        var s = NarutoStats.calculate(592, List.of(watch("one", start.minusDays(1))), start.plusDays(1));
        assertEquals(1, s.watched()); assertEquals(0, s.pace()); assertEquals(1, s.weekCount());
    }
    private List<NarutoWatch> spread(int perDay, int days) {
        List<NarutoWatch> watches = new ArrayList<>();
        for (int day = 0; day < days; day++) for (int n = 0; n < perDay; n++)
            watches.add(watch(day + "-" + n, start.plusDays(day)));
        return watches;
    }
    private NarutoWatch watch(String id, LocalDate day) { return new NarutoWatch(id, day, Instant.EPOCH, "friend"); }
}
