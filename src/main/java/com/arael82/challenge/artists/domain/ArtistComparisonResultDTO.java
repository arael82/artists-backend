package com.arael82.challenge.artists.domain;

import lombok.Data;

import java.util.Map;

@Data
public class ArtistComparisonResultDTO {
    private Long apiId;
    private String name;
    private int numberOfReleases;
    private int activeYears;
    private Map<String, Long> genreFrequency;

    public ArtistComparisonResultDTO(Long apiId, String name, int numberOfReleases,
                                     int activeYears, Map<String, Long> genreFrequency) {
        this.apiId = apiId;
        this.name = name;
        this.numberOfReleases = numberOfReleases;
        this.activeYears = activeYears;
        this.genreFrequency = genreFrequency;
    }
}
