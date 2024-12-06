package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final DiscogsApiClient discogsApiClient;

    public Object retrieveArtist(Integer artistId) {
        try {
            return discogsApiClient.getArtistById(artistId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving artist from Discogs API", e);
        }
    }

}
