package com.kirill.vinylencyclopedia.dto;

public class ApiVinylRecordDto {

    private Long id;
    private String artist;
    private String title;
    private String genre;
    private Integer releaseYear;
    private String labelName;
    private String collectionSection;
    private String vinylFormat;
    private String notes;
    private String coverImagePath;
    private String ownerUsername;

    public ApiVinylRecordDto() {
    }

    public ApiVinylRecordDto(
            Long id,
            String artist,
            String title,
            String genre,
            Integer releaseYear,
            String labelName,
            String collectionSection,
            String vinylFormat,
            String notes,
            String coverImagePath,
            String ownerUsername
    ) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.labelName = labelName;
        this.collectionSection = collectionSection;
        this.vinylFormat = vinylFormat;
        this.notes = notes;
        this.coverImagePath = coverImagePath;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getCollectionSection() {
        return collectionSection;
    }

    public String getVinylFormat() {
        return vinylFormat;
    }

    public String getNotes() {
        return notes;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public void setCollectionSection(String collectionSection) {
        this.collectionSection = collectionSection;
    }

    public void setVinylFormat(String vinylFormat) {
        this.vinylFormat = vinylFormat;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
