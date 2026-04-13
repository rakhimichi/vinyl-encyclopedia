package com.kirill.vinylencyclopedia.dto;

import com.kirill.vinylencyclopedia.domain.CollectionSection;
import com.kirill.vinylencyclopedia.domain.VinylFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VinylRecordFormDto {

    @NotBlank(message = "Artist is required")
    @Size(max = 255, message = "Artist must be at most 255 characters")
    private String artist;

    @NotBlank(message = "Album title is required")
    @Size(max = 255, message = "Album title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Genre is required")
    @Size(max = 255, message = "Genre must be at most 255 characters")
    private String genre;

    @Min(value = 1900, message = "Release year must be at least 1900")
    @Max(value = 2100, message = "Release year must be at most 2100")
    private Integer releaseYear;

    @Size(max = 255, message = "Label name must be at most 255 characters")
    private String labelName;

    @NotNull(message = "Collection section is required")
    private CollectionSection collectionSection;

    private VinylFormat vinylFormat;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;

    @NotBlank(message = "Cover image URL is required")
    @Size(max = 1000, message = "Cover image URL must be at most 1000 characters")
    private String coverImagePath;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public CollectionSection getCollectionSection() {
        return collectionSection;
    }

    public void setCollectionSection(CollectionSection collectionSection) {
        this.collectionSection = collectionSection;
    }

    public VinylFormat getVinylFormat() {
        return vinylFormat;
    }

    public void setVinylFormat(VinylFormat vinylFormat) {
        this.vinylFormat = vinylFormat;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }
}
