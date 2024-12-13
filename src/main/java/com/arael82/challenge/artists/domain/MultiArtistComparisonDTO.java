package com.arael82.challenge.artists.domain;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.List;

@Getter
@Log4j2
public class MultiArtistComparisonDTO {

    private final List<ArtistComparisonResultDTO> artists;

    private Long topArtistIdByReleases;

    private Long topArtistIdByActiveYears;

    public MultiArtistComparisonDTO(List<ArtistComparisonResultDTO> artists) {
        this.artists = artists;
        calculateTopReleaseArtist();
        calculateTopActiveYearsArtist();
    }

    /**
     * Calculate the artist with the most releases.
     */
    private void calculateTopReleaseArtist() {
        log.debug("Calculating top release artist");
        this.topArtistIdByReleases = artists.stream()
                .max(Comparator.comparingLong(ArtistComparisonResultDTO::getNumberOfReleases))
                .map(ArtistComparisonResultDTO::getApiId)
                .orElse(null);
    }

    /**
     * Calculate the artist with the most active years.
     */
    private void calculateTopActiveYearsArtist() {
        log.debug("Calculating top active years artist");
        this.topArtistIdByActiveYears = artists.stream()
                .max(Comparator.comparingInt(ArtistComparisonResultDTO::getActiveYears))
                .map(ArtistComparisonResultDTO::getApiId)
                .orElse(null);
    }
}
