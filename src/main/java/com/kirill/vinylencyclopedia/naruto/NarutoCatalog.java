package com.kirill.vinylencyclopedia.naruto;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class NarutoCatalog {
    public record Item(String id, int season, String kind, int number, String label) {}
    public record Group(String id, int season, String tags, String title, List<Item> items) {}
    private final List<Group> groups;
    private final Map<String, Item> items;

    public NarutoCatalog() {
        List<Group> loaded = new ArrayList<>();
        Map<String, Item> indexed = new LinkedHashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("naruto/watchlist.txt").getInputStream(), StandardCharsets.UTF_8))) {
            for (String line : reader.lines().filter(s -> !s.isBlank() && !s.startsWith("#")).toList()) {
                String[] fields = line.split("\\|");
                int season = Integer.parseInt(fields[0]);
                String spec = fields[2];
                List<Item> groupItems = new ArrayList<>();
                String title;
                if (spec.contains(" ")) {
                    String[] special = spec.split(" ");
                    String kind = special[0];
                    int number = Integer.parseInt(special[1]);
                    title = (kind.equals("OVA") ? "OVA " : "Фильм ") + number;
                    groupItems.add(new Item("s" + season + "-" + kind.toLowerCase(Locale.ROOT) + "-" + number,
                            season, kind, number, title));
                } else {
                    String[] range = spec.split("-");
                    int first = Integer.parseInt(range[0]);
                    int last = range.length == 2 ? Integer.parseInt(range[1]) : first;
                    title = first == last ? "Серия " + first : "Серии " + first + "–" + last;
                    for (int n = first; n <= last; n++) {
                        groupItems.add(new Item("s" + season + "-ep-" + n, season, "EPISODE", n, String.valueOf(n)));
                    }
                }
                for (Item item : groupItems) {
                    if (indexed.putIfAbsent(item.id(), item) != null) throw new IllegalStateException("Duplicate: " + item.id());
                }
                loaded.add(new Group("group-" + loaded.size(), season, fields[1], title, List.copyOf(groupItems)));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        groups = List.copyOf(loaded);
        items = Collections.unmodifiableMap(indexed);
    }
    public List<Group> groups() { return groups; }
    public Collection<Item> items() { return items.values(); }
    public boolean contains(String id) { return items.containsKey(id); }
    public int total() { return items.size(); }
}
