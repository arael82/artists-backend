package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import com.arael82.challenge.artists.api.client.DiscogsApiClientException;
import com.arael82.challenge.artists.api.client.domain.ApiResponseDto;
import com.arael82.challenge.artists.api.client.domain.ArtistResponseDto;
import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.data.repository.ArtistRepository;
import com.arael82.challenge.artists.service.exception.NotFoundException;
import com.arael82.challenge.artists.service.exception.UnexpectedServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_ID;
import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    DiscogsApiClient discogsApiClientMock;

    @Mock
    ArtistRepository artistRepositoryMock;

    private ArtistService artistService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<Artist> artistArgCaptor;

    @BeforeEach
    void setUp() {
        artistService = new ArtistService(discogsApiClientMock, artistRepositoryMock);
    }

    @Test
    void whenRetrieveArtistValidateDiscographyIsFound_ThenOk() throws IOException {
        //Given
        ArtistResponseDto artistResponseDto = new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        //When
        doReturn(artistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.getAlbums().add(new Album(artistEntity, 101L, "Thriller", "Main", 1982));

        doReturn(Optional.of(artistEntity))
                .when(artistRepositoryMock).findByApiId(any());

        String releaseResponseJson = "{\n" +
                "  \"pagination\": {\n" +
                "    \"page\": 1,\n" +
                "    \"pages\": 5,\n" +
                "    \"per_page\": 10,\n" +
                "    \"items\": 50\n" +
                "  },\n" +
                "  \"releases\": [\n" +
                "    {\n" +
                "      \"id\": 101,\n" +
                "      \"status\": \"Released\",\n" +
                "      \"type\": \"Album\",\n" +
                "      \"format\": \"Vinyl\",\n" +
                "      \"label\": \"Epic Records\",\n" +
                "      \"title\": \"Thriller\",\n" +
                "      \"role\": \"Main\",\n" +
                "      \"artist\": \"" + TEST_ARTIST_NAME + "\",\n" +
                "      \"year\": 1982,\n" +
                "      \"thumb\": \"https://example.com/thriller.jpg\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 102,\n" +
                "      \"status\": \"Released\",\n" +
                "      \"type\": \"Single\",\n" +
                "      \"format\": \"CD\",\n" +
                "      \"label\": \"Columbia\",\n" +
                "      \"title\": \"Shape of You\",\n" +
                "      \"role\": \"Main\",\n" +
                "      \"artist\": \"" + TEST_ARTIST_NAME + "\",\n" +
                "      \"year\": 2017,\n" +
                "      \"thumb\": \"https://example.com/shape_of_you.jpg\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 103,\n" +
                "      \"status\": \"Upcoming\",\n" +
                "      \"type\": \"EP\",\n" +
                "      \"format\": \"Digital\",\n" +
                "      \"label\": \"Indie Label\",\n" +
                "      \"title\": \"New Horizons\",\n" +
                "      \"role\": \"Contributor\",\n" +
                "      \"artist\": \"" + TEST_ARTIST_NAME + "\",\n" +
                "      \"year\": 2024,\n" +
                "      \"thumb\": \"https://example.com/new_horizons.jpg\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        ApiResponseDto discographyApiResponse = objectMapper.readValue(releaseResponseJson, ApiResponseDto.class);
        doReturn(discographyApiResponse)
                .when(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);

        Artist updatedArtist = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        updatedArtist.getAlbums().addAll(discographyApiResponse.releases().stream()
                .map(album -> new Album(updatedArtist, album.id(), album.title(), album.role(), album.year()))
                        .toList());

        doReturn(artistEntity, updatedArtist)
                .when(artistRepositoryMock).save(any());
        //Do
        Artist retrievedArtist = artistService.retrieveArtist(TEST_ARTIST_ID);

        //Assert and Verify
        assertEquals(TEST_ARTIST_ID, retrievedArtist.getApiId());
        assertEquals(TEST_ARTIST_NAME, retrievedArtist.getName());
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(artistRepositoryMock, times(2)).save(artistArgCaptor.capture());
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        var caughtToSave = artistArgCaptor.getAllValues().get(0);
        var caughtUpdated = artistArgCaptor.getAllValues().get(1);
        assertEquals(TEST_ARTIST_NAME, caughtToSave.getName());
        assertEquals(TEST_ARTIST_ID, caughtToSave.getApiId());
        assertEquals(TEST_ARTIST_NAME, caughtUpdated.getName());
        assertEquals(TEST_ARTIST_ID, caughtUpdated.getApiId());
        assertEquals(discographyApiResponse.releases().size(), caughtUpdated.getAlbums().size());
        artistArgCaptor.getValue().getAlbums().forEach(album -> assertTrue(discographyApiResponse.releases().stream()
                .anyMatch(release -> release.id().equals(album.getApiId()))));
        verifyNoMoreInteractions(artistRepositoryMock, discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateDiscographyNotFound_ThenOk() throws IOException {
        //Given
        ArtistResponseDto artistResponseDto = new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        //When
        doReturn(artistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

        doReturn(artistEntity)
                .when(artistRepositoryMock).save(any());

        doThrow(new DiscogsApiClientException("Not found", HttpStatus.NOT_FOUND.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        Artist retrievedArtist = artistService.retrieveArtist(TEST_ARTIST_ID);

        //Assert and Verify
        assertEquals(TEST_ARTIST_ID, retrievedArtist.getApiId());
        assertEquals(TEST_ARTIST_NAME, retrievedArtist.getName());
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(artistRepositoryMock).save(artistArgCaptor.capture());
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        assertEquals(TEST_ARTIST_ID, artistArgCaptor.getValue().getApiId());
        assertEquals(TEST_ARTIST_NAME, artistArgCaptor.getValue().getName());
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistDiscographyReturns504NotFound_ThenThrowException() throws IOException {
        //Given
        ArtistResponseDto artistResponseDto = new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        //When
        doReturn(artistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

         doThrow(new DiscogsApiClientException("Timeout", HttpStatus.GATEWAY_TIMEOUT.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistDiscographyValidateNPE_ThenThrowException() throws IOException {
        //Given
        ArtistResponseDto artistResponseDto = new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        //When
        doReturn(artistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

        doThrow(new NullPointerException())
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(artistRepositoryMock, discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateOtherError_ThenThrowException() throws IOException {
        //Given
        ArtistResponseDto artistResponseDto = new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        //When
        doReturn(artistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doThrow(new RuntimeException("DB error"))
                .when(artistRepositoryMock).findByApiId(any());

        //Do
        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        //Assert and Verify
        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(artistRepositoryMock, discogsApiClientMock);
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
}