package com.arael82.challenge.artists.domain;

import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Log4j2
public class ArtistComparisonResultDTO {

    private final Long apiId;
    private final String name;
    private long numberOfReleases;
    private int activeYears;
    private Map<String, Long> genreFrequency;

    public ArtistComparisonResultDTO(Artist source) {
        this.apiId = source.getApiId();
        this.name = source.getName();
        calculateStats(source);
    }

    /**
     * Calculate stats for the artist, based on the albums they have released.
     * @param source the artist to calculate stats for
     */
    private void calculateStats(Artist source) {
        log.info("Calculating stats for artist {}", source.getName());

        log.debug("Calculating number of releases for artist {}", source.getName());
        this.numberOfReleases = source.getAlbums().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive())).count();

        log.debug("Calculating active years for artist {}", source.getName());
        this.activeYears = source.getAlbums().stream()
                .mapToInt(Album::getYear).max().orElse(0) - source.getAlbums().stream()
                .mapToInt(Album::getYear).min().orElse(0);

        log.debug("Calculating genre frequency for artist {}", source.getName());

        log.debug("Calculating genre frequency for artist {}", source.getName());
        this.genreFrequency = source.getAlbums().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .map(Album::getGenre)
                .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()));
    }
}
