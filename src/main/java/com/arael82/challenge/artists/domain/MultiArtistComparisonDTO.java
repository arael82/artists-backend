package com.arael82.challenge.artists.domain;

import lombok.Data;

import java.util.List;

@Data
public class MultiArtistComparisonDTO {

    private List<ArtistComparisonResultDTO> artists;

    private Long topArtistIdByReleases;

    private Long topArtistIdByActiveYears;

    public MultiArtistComparisonDTO(List<ArtistComparisonResultDTO> artists) {
        this.artists = artists;
        calculateTopReleaseArtist();
        calculateTopActiveYearsArtist();
    }

    private void calculateTopReleaseArtist() {

    }

    private void calculateTopActiveYearsArtist() {

    }
}
