package com.arael82.challenge.artists.controller;

import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
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

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/{artistId}")
    public ResponseEntity<Artist> getArtist(
            @PathVariable Long artistId) {
        return ResponseEntity.ok(artistService.retrieveArtist(artistId));
    }

    @GetMapping("/{artistId}/albums")
    public ResponseEntity<Page<Album>> getAlbumsByArtistId(
            @PathVariable Long artistId,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") @Nonnull Integer page,
            @RequestParam(defaultValue = "20") @Nonnull Integer size) {
        return ResponseEntity.ok(artistService.searchAlbums(artistId,genre, title, null, page, size));
    }

}
