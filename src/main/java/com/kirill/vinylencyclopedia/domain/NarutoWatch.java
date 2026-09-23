package com.kirill.vinylencyclopedia.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "naruto_watches")
public class NarutoWatch {
    @Id
    @Column(length = 40)
    private String itemId;
    @Column(nullable = false)
    private LocalDate watchedOn;
    @Column(nullable = false)
    private Instant markedAt;
    @Column(nullable = false)
    private String markedBy;
    @Version
    private Long version;

    protected NarutoWatch() {}
    public NarutoWatch(String itemId, LocalDate watchedOn, Instant markedAt, String markedBy) {
        this.itemId = itemId;
        this.watchedOn = watchedOn;
        this.markedAt = markedAt;
        this.markedBy = markedBy;
    }
    public String getItemId() { return itemId; }
    public LocalDate getWatchedOn() { return watchedOn; }
    public Instant getMarkedAt() { return markedAt; }
    public String getMarkedBy() { return markedBy; }
}
