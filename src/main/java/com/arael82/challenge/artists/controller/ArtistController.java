package com.arael82.challenge.artists.controller;

import com.arael82.challenge.artists.domain.AlbumResponseDto;
import com.arael82.challenge.artists.domain.ArtistResponseDto;
import com.arael82.challenge.artists.domain.MultiArtistComparison;
import com.arael82.challenge.artists.service.ArtistService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    /**
     * Get artist by ID.
     * @param artistId Artist ID.
     * @return Artist information.
     */
    @GetMapping("/{artistId}")
    public ResponseEntity<ArtistResponseDto> getArtist(
            @PathVariable Long artistId) {
        return ResponseEntity.ok(artistService.retrieveArtist(artistId));
    }

    /**
     * Get albums by artist ID.
     * @param artistId Artist ID.
     * @param genre Genre to filter by.
     * @param title Title to filter by.
     * @param page Page number.
     * @param size Page size.
     * @return Page of albums.
     */
    @GetMapping("/{artistId}/albums")
    public ResponseEntity<Page<AlbumResponseDto>> getAlbumsByArtistId(
            @PathVariable Long artistId,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") @Nonnull Integer page,
            @RequestParam(defaultValue = "20") @Nonnull Integer size) {
        return ResponseEntity.ok(artistService.searchAlbums(artistId,genre, title, year, page, size));
    }

    /**
     * Compare multiple artists.
     * @param artistIds List of artist IDs to compare.
     * @return Comparison result.
     */
    @GetMapping("/compare")
    public ResponseEntity<MultiArtistComparison> compareArtists(
            @RequestParam List<Long> artistIds) {
        return ResponseEntity.ok(artistService.compareArtists(artistIds));
    }

}
