package com.arael82.challenge.artists.controller;

import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.domain.MultiArtistComparisonDTO;
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
    public ResponseEntity<Artist> getArtist(
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
    public ResponseEntity<Page<Album>> getAlbumsByArtistId(
            @PathVariable Long artistId,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") @Nonnull Integer page,
            @RequestParam(defaultValue = "20") @Nonnull Integer size) {
        return ResponseEntity.ok(artistService.searchAlbums(artistId,genre, title, null, page, size));
    }

    /**
     * Compare multiple artists.
     * @param artistIds List of artist IDs to compare.
     * @return Comparison result.
     */
    @GetMapping("/compare")
    public ResponseEntity<MultiArtistComparisonDTO> compareArtists(
            @RequestParam List<Long> artistIds) {
        return ResponseEntity.ok(artistService.compareArtists(artistIds));
    }

}
