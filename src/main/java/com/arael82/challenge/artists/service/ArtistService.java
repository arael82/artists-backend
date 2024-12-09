package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import com.arael82.challenge.artists.api.client.DiscogsApiClientException;
import com.arael82.challenge.artists.api.client.domain.ApiResponseDto;
import com.arael82.challenge.artists.api.client.domain.ArtistResponseDto;
import com.arael82.challenge.artists.service.exception.NotFoundException;
import com.arael82.challenge.artists.service.exception.UnexpectedServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final DiscogsApiClient discogsApiClient;

    /**
     * Retrieve artist from Discogs API.
     * @param artistId Artist ID.
     * @return The artist information, if there is any.
     */
    public ArtistResponseDto retrieveArtist(Integer artistId) {
        try {
            return discogsApiClient.getArtistById(artistId);
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
     * Retrieve discography from Discogs API for a given artist, by artist ID.
     * @param artistId Artist ID.
     * @return The discography information, if there is any.
     */
    public ApiResponseDto retrieveDiscography(Integer artistId) {
        try {
            return discogsApiClient.getReleasesByArtistId(artistId);
        } catch (DiscogsApiClientException dacEx) {
            if (dacEx.code == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(String.format("Discography for artist not found (%s).", artistId));
            } else {
                throw new UnexpectedServiceException(dacEx);
            }
        } catch (UnexpectedServiceException usEx) {
            throw usEx;
        } catch (Exception ex) {
            throw new UnexpectedServiceException(ex);
        }
    }

}
