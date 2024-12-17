package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import com.arael82.challenge.artists.api.client.DiscogsApiClientException;
import com.arael82.challenge.artists.api.client.domain.DiscogsApiArtistResponseDto;
import com.arael82.challenge.artists.api.client.domain.DiscogsApiReleaseResponseDto;
import com.arael82.challenge.artists.api.client.domain.DiscogsApiResponseDto;
import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.data.repository.AlbumRepository;
import com.arael82.challenge.artists.data.repository.ArtistRepository;
import com.arael82.challenge.artists.data.specification.AlbumSpecifications;
import com.arael82.challenge.artists.domain.ArtistComparisonResultDTO;
import com.arael82.challenge.artists.domain.MultiArtistComparisonDTO;
import com.arael82.challenge.artists.service.exception.NotFoundException;
import com.arael82.challenge.artists.service.exception.UnexpectedServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistService {

    private final DiscogsApiClient discogsApiClient;

    private final ArtistRepository artistRepository;

    private final AlbumRepository albumRepository;

    /**
     * Retrieve artist from Discogs API.
     * @param artistId Artist ID.
     * @return The artist information, if there is any.
     */
    @Transactional
    public Artist retrieveArtist(Long artistId) {
        try {
            var artistResponseFromApi = discogsApiClient.getArtistById(artistId);
            var artistEntity = getArtistFromDb(artistResponseFromApi);
            log.info("Processing discography for artist ({}: {})", artistEntity.getId(), artistEntity.getName());
            var discographyResponseFromApi = retrieveDiscography(artistId);
            log.info("Updating discography for artist ({}: {})", artistEntity.getId(), artistEntity.getName());
            updateAlbumsList(artistEntity, discographyResponseFromApi);
            log.debug("Persisting updated artist with discography ({}: {})", artistEntity.getId(), artistEntity.getName());
            var updatedArtist = artistRepository.save(artistEntity);
            log.info("Successfully persisted artist with discography ({}: {})", updatedArtist.getId(), updatedArtist.getName());
            return updatedArtist;
        } catch (DiscogsApiClientException dacEx) {
            if(dacEx.code == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(String.format("Artist not found (%s).", artistId));
            } else {
                throw new UnexpectedServiceException(dacEx);
            }
        } catch (UnexpectedServiceException usEx) {
            throw usEx;
        } catch (Exception ex) {
            throw new UnexpectedServiceException(ex);
        }
    }

    /**
     * Search for albums by artist, with optional filters.
     * @param artistApiId Artist ID.
     * @param genre Genre filter.
     * @param title Title filter.
     * @param year Year filter.
     * @param page Page number.
     * @param size Page size.
     * @return A page of albums that match the search criteria.
     */
    public Page<Album> searchAlbums(
            long artistApiId,
            String genre,
            String title,
            Integer year,
            int page,
            int size) {

        log.debug("Preparing album search for artist ({}) with filters: genre={}, title={}, year={}",
                artistApiId, genre, title, year);

        Specification<Album> spec = Specification
                .where(AlbumSpecifications.hasArtistApiId(artistApiId))
                .and(AlbumSpecifications.isActive(true));

        if (StringUtils.isNotBlank(genre)) {
            spec = spec.and(AlbumSpecifications.hasGenre(genre));
        }

        if (StringUtils.isNotBlank(title)) {
            spec = spec.and(AlbumSpecifications.titleContains(title));
        }

        if (year != null) {
            spec = spec.and(AlbumSpecifications.hasYear(year));
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Order.asc("year"), Sort.Order.asc("title"))
        );

        log.info("Searching albums for artist ({}) with filters: genre={}, title={}, year={}",
                artistApiId, genre, title, year);

        return albumRepository.findAll(spec, pageable);

    }

    /**
     * Compare multiple artists by ID.
     * @param artistIds List of artist IDs.
     * @return Comparison results for the artists.
     */
    public MultiArtistComparisonDTO compareArtists(List<Long> artistIds) {
        try {
            log.info("Performing search of artists for comparison: {}", artistIds);
            var artists = artistRepository.findAllById(artistIds);

            if (artists.isEmpty()) {
                var message = String.format("No artists found for comparison: %s", artistIds);
                log.info(message);
                throw new NotFoundException(message);
            }

            log.info("Comparing artists: {}", artists.stream().map(Artist::getId).toList());

            return new MultiArtistComparisonDTO(artists.stream()
                    .map(ArtistComparisonResultDTO::new)
                    .toList());
        } catch (NotFoundException nfEx) {
            throw nfEx;
        } catch (Exception ex) {
            log.error("Unexpected error while comparing artists.", ex);
            throw new UnexpectedServiceException(ex);
        }
    }

    /**
     * Retrieve discography from Discogs API for a given artist, by artist ID.
     * @param artistId Artist ID.
     * @return The discography information, if there is any.
     */
    private DiscogsApiResponseDto retrieveDiscography(Long artistId) {
        try {
            log.debug("Retrieving discography from API for artist ({})", artistId);
            return discogsApiClient.getReleasesByArtistId(artistId);
        } catch (DiscogsApiClientException dacEx) {
            if (dacEx.code == HttpStatus.NOT_FOUND) {
                log.info("Discography not found for artist ({})", artistId);
                return null;
            } else {
                throw new UnexpectedServiceException(dacEx);
            }
        } catch (UnexpectedServiceException usEx) {
            throw usEx;
        } catch (Exception ex) {
            throw new UnexpectedServiceException(ex);
        }
    }

    /**
     * Persist artist information in the database, based on the information retrieved from Discogs API.
     * @param source Artist information retrieved from Discogs API.
     * @return The artist information persisted in the database.
     */
    private Artist getArtistFromDb(DiscogsApiArtistResponseDto source) {
        try {

            Optional<Artist> existingArtist = artistRepository.findByApiId(source.id());

            if(existingArtist.isEmpty()){
                log.info("Artist not found, creating new Artist entity ({}: {})", source.id(), source.name());
                return new Artist(source.id(), source.name());
            }

            log.info("Artist found, returning existing existingArtist ({}: {})", source.id(), source.name());
            Artist artistToUpdate = existingArtist.get();
            artistToUpdate.setName(source.name());
            return artistRepository.save(artistToUpdate);

        } catch (Exception ex) {
            throw new UnexpectedServiceException(ex);
        }
    }

    private void updateAlbumsList(Artist artist, DiscogsApiResponseDto discography) {
        if (discography == null) {
            log.debug("Soft-deleting any discography for artist ({}: {})", artist.getId(), artist.getName());
            artist.getAlbums().forEach(album -> album.setActive(false));
            return;
        }

        log.debug("Processing discography for artist ({}: {})", artist.getId(), artist.getName());

        Map<Long, DiscogsApiReleaseResponseDto> entriesFromApi = discography.releases().stream()
                .collect(Collectors.toMap(DiscogsApiReleaseResponseDto::id, Function.identity()));

        Map<Long, Album> savedEntries = artist.getAlbums().stream()
                .collect(Collectors.toMap(Album::getApiId, Function.identity()));

        for(Album album : artist.getAlbums()) {

            log.info("Refreshing Album based on API response ({}: {})", album.getApiId(), album.getTitle());
            album.setActive(entriesFromApi.containsKey(album.getApiId()));
            album.setTitle(entriesFromApi.get(album.getApiId()).title());
            album.setGenre(entriesFromApi.get(album.getApiId()).role());
            album.setYear(entriesFromApi.get(album.getApiId()).year());

        }

        artist.getAlbums().addAll(
                entriesFromApi.entrySet().stream()
                        .filter(entry -> savedEntries.get(entry.getKey()) == null)
                        .map(entry -> new Album(artist, entry.getKey(), entry.getValue().title(), entry.getValue().role(), entry.getValue().year()))
                        .toList());

    }
}
