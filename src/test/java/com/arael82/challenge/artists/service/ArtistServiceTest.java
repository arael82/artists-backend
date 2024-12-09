package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import com.arael82.challenge.artists.api.client.DiscogsApiClientException;
import com.arael82.challenge.artists.service.exception.NotFoundException;
import com.arael82.challenge.artists.service.exception.UnexpectedServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    DiscogsApiClient discogsApiClientMock;

    private ArtistService artistService;

    @BeforeEach
    void setUp() {
        artistService = new ArtistService(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateIsFound_ThenOk() {
    }

    @Test
    void whenRetrieveArtistValidateNotFound_ThenThrowException() throws IOException {

        //When
        doThrow(new DiscogsApiClientException("Not found", HttpStatus.NOT_FOUND.value()))
                .when(discogsApiClientMock).getArtistById(any());

        //Do
        assertThrows(NotFoundException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateApiFailed_ThenThrowException() throws IOException {

        //When
        doThrow(new DiscogsApiClientException("Timeout", HttpStatus.GATEWAY_TIMEOUT.value()))
                .when(discogsApiClientMock).getArtistById(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateUnexpectedError_ThenThrowException() throws IOException {

        //When
        doThrow(new RuntimeException("Unexpected error"))
                .when(discogsApiClientMock).getArtistById(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }


    @Test
    void whenRetrieveDiscographyValidateIsFound_ThenOk() {
    }

    @Test
    void whenRetrieveDiscographyValidateNotFound_ThenThrowException() throws IOException {

        //When
        doThrow(new DiscogsApiClientException("Not found", HttpStatus.NOT_FOUND.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        assertThrows(NotFoundException.class, () -> artistService.retrieveDiscography(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveDiscographyValidateApiFailed_ThenThrowException() throws IOException {

        //When
        doThrow(new DiscogsApiClientException("Timeout", HttpStatus.GATEWAY_TIMEOUT.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveDiscography(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveDiscographyValidateUnexpectedError_ThenThrowException() throws IOException {

        //When
        doThrow(new RuntimeException("Unexpected error"))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveDiscography(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

}