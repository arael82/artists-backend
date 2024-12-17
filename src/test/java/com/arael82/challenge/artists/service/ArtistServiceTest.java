package com.arael82.challenge.artists.service;

import com.arael82.challenge.artists.api.client.DiscogsApiClient;
import com.arael82.challenge.artists.api.client.DiscogsApiClientException;
import com.arael82.challenge.artists.api.client.domain.DiscogsApiArtistResponseDto;
import com.arael82.challenge.artists.api.client.domain.DiscogsApiResponseDto;
import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.data.repository.AlbumRepository;
import com.arael82.challenge.artists.data.repository.ArtistRepository;
import com.arael82.challenge.artists.domain.AlbumResponseDto;
import com.arael82.challenge.artists.domain.ArtistResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_ID;
import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private DiscogsApiClient discogsApiClientMock;

    @Mock
    private ArtistRepository artistRepositoryMock;

    @Mock
    private AlbumRepository albumRepositoryMock;

    private ArtistService artistService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<Artist> artistArgCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableArgCaptor;

    @Captor
    private ArgumentCaptor<Specification<Album>> albumSpecificationsCaptor;

    @BeforeEach
    void setUp() {
        artistService = new ArtistService(discogsApiClientMock, artistRepositoryMock, albumRepositoryMock);
    }

    @Test
    void whenRetrieveArtistValidateDiscographyIsFound_ThenOk() throws IOException {

        DiscogsApiArtistResponseDto discogsApiArtistResponseDto = new DiscogsApiArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        doReturn(discogsApiArtistResponseDto)
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

        DiscogsApiResponseDto discographyApiResponse = objectMapper.readValue(releaseResponseJson, DiscogsApiResponseDto.class);
        doReturn(discographyApiResponse)
                .when(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);

        Artist updatedArtist = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        updatedArtist.getAlbums().addAll(discographyApiResponse.releases().stream()
                .map(album -> new Album(updatedArtist, album.id(), album.title(), album.role(), album.year()))
                        .toList());

        doReturn(artistEntity, updatedArtist)
                .when(artistRepositoryMock).save(any());

        ArtistResponseDto retrievedArtist = artistService.retrieveArtist(TEST_ARTIST_ID);

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

        DiscogsApiArtistResponseDto discogsApiArtistResponseDto = new DiscogsApiArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        doReturn(discogsApiArtistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

        doReturn(artistEntity)
                .when(artistRepositoryMock).save(any());

        doThrow(new DiscogsApiClientException("Not found", HttpStatus.NOT_FOUND.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        ArtistResponseDto retrievedArtist = artistService.retrieveArtist(TEST_ARTIST_ID);

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

        DiscogsApiArtistResponseDto discogsApiArtistResponseDto = new DiscogsApiArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        doReturn(discogsApiArtistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

         doThrow(new DiscogsApiClientException("Timeout", HttpStatus.GATEWAY_TIMEOUT.value()))
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistDiscographyValidateNPE_ThenThrowException() throws IOException {

        DiscogsApiArtistResponseDto discogsApiArtistResponseDto = new DiscogsApiArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        doReturn(discogsApiArtistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doReturn(Optional.empty())
                .when(artistRepositoryMock).findByApiId(any());

        Artist artistEntity = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artistEntity.setApiId(TEST_ARTIST_ID);

        doThrow(new NullPointerException())
                .when(discogsApiClientMock).getReleasesByArtistId(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verify(artistRepositoryMock).findByApiId(TEST_ARTIST_ID);
        verify(discogsApiClientMock).getReleasesByArtistId(TEST_ARTIST_ID);
        verifyNoMoreInteractions(artistRepositoryMock, discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateOtherError_ThenThrowException() throws IOException {

        DiscogsApiArtistResponseDto discogsApiArtistResponseDto = new DiscogsApiArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME);

        doReturn(discogsApiArtistResponseDto)
                .when(discogsApiClientMock).getArtistById(any());
        doThrow(new RuntimeException("DB error"))
                .when(artistRepositoryMock).findByApiId(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(artistRepositoryMock, discogsApiClientMock);
    }


    @Test
    void whenRetrieveArtistValidateNotFound_ThenThrowException() throws IOException {

        doThrow(new DiscogsApiClientException("Not found", HttpStatus.NOT_FOUND.value()))
                .when(discogsApiClientMock).getArtistById(any());

        assertThrows(NotFoundException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateApiFailed_ThenThrowException() throws IOException {

        doThrow(new DiscogsApiClientException("Timeout", HttpStatus.GATEWAY_TIMEOUT.value()))
                .when(discogsApiClientMock).getArtistById(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @Test
    void whenRetrieveArtistValidateUnexpectedError_ThenThrowException() throws IOException {

        doThrow(new RuntimeException("Unexpected error"))
                .when(discogsApiClientMock).getArtistById(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.retrieveArtist(TEST_ARTIST_ID));

        verify(discogsApiClientMock).getArtistById(TEST_ARTIST_ID);
        verifyNoMoreInteractions(discogsApiClientMock);
    }

    @SuppressWarnings({"unchecked", "unused"})
    @Test
    void whenSearchAlbumsAllParamsThenValidateNotNull_ThenOk() {

        int testPage = 1;
        int testPageSize = 20;

        Artist artist = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artist.getAlbums().addAll(Arrays.asList(
                new Album(artist, 101L, "Thriller", "Main", 1982),
                new Album(artist, 102L, "Shape of You", "Main", 2017),
                new Album(artist, 103L, "New Horizons", "Contributor", 2024)));
        Pageable pageable = PageRequest.of(testPage, testPageSize);
        List<Album> albumList = artist.getAlbums();

        Page<Album> albumPage = new PageImpl<>(albumList, pageable, albumList.size());

        doReturn(albumPage).when(albumRepositoryMock).findAll(any(Specification.class), any(Pageable.class));

        Page<AlbumResponseDto> albums = artistService.searchAlbums(
                TEST_ARTIST_ID, "Album", "Thriller", 2024, testPage, testPageSize);

        verify(albumRepositoryMock).findAll(albumSpecificationsCaptor.capture(),
                pageableArgCaptor.capture());
        assertEquals(0, pageableArgCaptor.getValue().getPageNumber());
        verifyNoMoreInteractions(artistRepositoryMock, albumRepositoryMock);
    }

    @SuppressWarnings({"unchecked", "unused"})
    @Test
    void whenSearchAlbumsNoOptionalParamsThenValidateNotNull_ThenOk() {

        int testPage = 1;
        int testPageSize = 20;

        Artist artist = new Artist(TEST_ARTIST_ID, TEST_ARTIST_NAME);
        artist.getAlbums().addAll(Arrays.asList(
                new Album(artist, 101L, "Thriller", "Main", 1982),
                new Album(artist, 102L, "Shape of You", "Main", 2017),
                new Album(artist, 103L, "New Horizons", "Contributor", 2024)));
        Pageable pageable = PageRequest.of(testPage, testPageSize);
        List<Album> albumList = artist.getAlbums();

        Page<Album> albumPage = new PageImpl<>(albumList, pageable, albumList.size());

        doReturn(albumPage).when(albumRepositoryMock).findAll(any(Specification.class), any(Pageable.class));

        Page<AlbumResponseDto> albums = artistService.searchAlbums(
                TEST_ARTIST_ID, null, null, null, testPage, testPageSize);

        verify(albumRepositoryMock).findAll(albumSpecificationsCaptor.capture(),
                pageableArgCaptor.capture());
        assertEquals(0, pageableArgCaptor.getValue().getPageNumber());
        verifyNoMoreInteractions(artistRepositoryMock, albumRepositoryMock);
    }

    @Test
    void whenCompareArtistsValidateNotFound_thenThrowException() {

        List<Long> artistIds = List.of(999L, 1000L);

        doReturn(Collections.emptyList()).when(artistRepositoryMock).findAllByApiIdIn(any());

        assertThrows(NotFoundException.class, () -> artistService.compareArtists(artistIds));

        verify(artistRepositoryMock).findAllByApiIdIn(artistIds);
        verifyNoMoreInteractions(artistRepositoryMock);
    }

    @Test
    void whenCompareArtistsValidateUnexpectedError_thenThrowException() {

        List<Long> artistIds = List.of(101L, 102L);

        doThrow(new RuntimeException("DB is down")).when(artistRepositoryMock).findAllByApiIdIn(any());

        assertThrows(UnexpectedServiceException.class, () -> artistService.compareArtists(artistIds));

        verify(artistRepositoryMock, times(1)).findAllByApiIdIn(artistIds);
        verifyNoMoreInteractions(artistRepositoryMock);
    }

    @Test
    void whenCompareArtistsValidateSuccess_ThenOk() {

        List<Long> artistIds = List.of(101L, 102L);

        Artist artist1 = new Artist(101L, "Artist 101");
        Artist artist2 = new Artist(102L, "Artist 102");

        doReturn(List.of(artist1, artist2)).when(artistRepositoryMock).findAllByApiIdIn(any());

        var result = artistService.compareArtists(artistIds);

        assertNotNull(result);

        assertEquals(2, result.getArtists().size());
        assertEquals(artist1.getApiId(), result.getArtists().get(0).getApiId());
        assertEquals(artist1.getName(), result.getArtists().get(0).getName());
        assertEquals(artist2.getApiId(), result.getArtists().get(1).getApiId());
        assertEquals(artist2.getName(), result.getArtists().get(1).getName());

        verify(artistRepositoryMock).findAllByApiIdIn(artistIds);
        verifyNoMoreInteractions(artistRepositoryMock);
    }
}