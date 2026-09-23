package com.kirill.vinylencyclopedia.naruto;

import com.kirill.vinylencyclopedia.domain.NarutoWatch;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class NarutoStats {
    public static final LocalDate START = LocalDate.of(2026, 9, 24);
    public static final LocalDate DEADLINE = START.plusMonths(12);
    public static final ZoneId ZONE = ZoneId.of("Europe/Zurich");
    public static final double TARGET = 3.0;
    public record Day(LocalDate date, long count) {}
    public record Summary(int total, int watched, int remaining, double percent, double pace,
                          long todayCount, long weekCount, LocalDate predictedFinish, LocalDate baselineFinish,
                          LocalDate start, LocalDate deadline, LocalDate today, long daysLeft,
                          double requiredPace, String status, List<Day> activity, int paceWindowDays) {}

    private NarutoStats() {}

    public static Summary calculate(int total, List<NarutoWatch> watches, LocalDate today) {
        int watched = watches.size();
        int remaining = Math.max(0, total - watched);
        Map<LocalDate, Long> counts = new TreeMap<>();
        watches.forEach(w -> counts.merge(w.getWatchedOn(), 1L, Long::sum));
        LocalDate windowStart = today.minusDays(6).isBefore(START) ? START : today.minusDays(6);
        int windowDays = (int) Math.max(0, ChronoUnit.DAYS.between(windowStart, today) + 1);
        long paceCount = counts.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(windowStart) && !e.getKey().isAfter(today))
                .mapToLong(Map.Entry::getValue).sum();
        // Until the first viewing on launch day, the forecast uses the requested 3/day plan.
        double pace = today.isBefore(START) || (today.equals(START) && paceCount == 0)
                ? TARGET : (windowDays > 0 ? (double) paceCount / windowDays : TARGET);
        long week = counts.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(today.minusDays(6)) && !e.getKey().isAfter(today))
                .mapToLong(Map.Entry::getValue).sum();
        LocalDate completion = watches.stream().map(NarutoWatch::getWatchedOn).max(LocalDate::compareTo).orElse(null);
        LocalDate predicted = null;
        if (remaining == 0) {
            predicted = completion;
        } else if (pace > 0) {
            long days = (long) Math.ceil(remaining / pace);
            boolean initialPlan = today.isBefore(START) || (today.equals(START) && paceCount == 0);
            predicted = initialPlan ? START.plusDays(Math.max(0, days - 1)) : today.plusDays(days);
        }
        String status;
        if (remaining == 0 && completion != null && !completion.isAfter(DEADLINE)) status = "WON";
        else if (today.isAfter(DEADLINE)) status = "LOST";
        else if (today.isBefore(START)) status = "NOT_STARTED";
        else if (predicted == null) status = "PAUSED";
        else if (predicted.isAfter(DEADLINE)) status = "AT_RISK";
        else status = "ON_TRACK";
        long availableDays = Math.max(0, ChronoUnit.DAYS.between(today.isBefore(START) ? START : today, DEADLINE) + 1);
        List<Day> activity = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            activity.add(new Day(date, counts.getOrDefault(date, 0L)));
        }
        return new Summary(total, watched, remaining, total == 0 ? 0 : 100.0 * watched / total,
                pace, counts.getOrDefault(today, 0L), week, predicted,
                START.plusDays((long) Math.ceil(total / TARGET) - 1), START, DEADLINE, today,
                Math.max(0, ChronoUnit.DAYS.between(today, DEADLINE)),
                availableDays == 0 ? 0 : (double) remaining / availableDays, status, List.copyOf(activity), windowDays);
    }
}
