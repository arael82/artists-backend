package com.arael82.challenge.artists.controller;

import com.arael82.challenge.artists.api.client.domain.ArtistResponseDto;
import com.arael82.challenge.artists.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/{artistId}")
    public ResponseEntity<ArtistResponseDto> getArtist(@PathVariable Integer artistId) {
        return ResponseEntity.ok(artistService.retrieveArtist(artistId));
    }

    @GetMapping("/{artistId}/discography")
    public ResponseEntity<?> getDiscography(@PathVariable Integer artistId) {
        return ResponseEntity.ok(artistService.retrieveDiscography(artistId));
    }
}
