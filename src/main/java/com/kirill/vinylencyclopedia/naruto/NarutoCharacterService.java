package com.kirill.vinylencyclopedia.naruto;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class NarutoCharacterService {
    public record Character(String name, String url) {}
    public record DailyCharacter(String name, String url, LocalDate date, int poolSize) {}
    private final List<Character> characters;

    public NarutoCharacterService() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("naruto/characters.tsv").getInputStream(), StandardCharsets.UTF_8))) {
            characters = reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(line -> line.split("\t"))
                    .map(parts -> new Character(parts[0], parts[1])).toList();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        if (characters.isEmpty()) throw new IllegalStateException("Character catalog is empty");
    }

    public DailyCharacter forDay(String username, LocalDate date) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(("naruto-day-v1:" + username + ":" + date).getBytes(StandardCharsets.UTF_8));
            int index = Math.floorMod(java.nio.ByteBuffer.wrap(hash).getInt(), characters.size());
            Character character = characters.get(index);
            return new DailyCharacter(character.name(), character.url(), date, characters.size());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
