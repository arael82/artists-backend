package com.arael82.challenge.artists.api.client;

import com.arael82.challenge.artists.api.client.domain.ApiResponseDto;
import com.arael82.challenge.artists.api.client.domain.ArtistResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_ID;
import static com.arael82.challenge.artists.TestConstants.TEST_ARTIST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class DiscogsApiClientTest {

    @Mock
    private OkHttpClient httpClientMock;

    @Mock
    private Call callMock;

    @Mock
    private Response responseMock;

    private final ObjectMapper objectMapper = new ObjectMapper();

    DiscogsApiClient discogsApiClient;

    @BeforeEach
    void setUp() throws IOException {
        discogsApiClient = new DiscogsApiClient("http://localhost:8080", "token", httpClientMock, objectMapper);
        doReturn(callMock).when(httpClientMock).newCall(any());
        doReturn(responseMock).when(callMock).execute();
    }

    @Test
    void whenGetArtistByIdValidateIsFound_ThenOk() throws IOException {

        // When
        doReturn(true).when(responseMock).isSuccessful();

        String artistResponseJson = "{\"id\":" + TEST_ARTIST_ID + ",\"name\":\""+ TEST_ARTIST_NAME +"\"}";
        ResponseBody testResponseBody = ResponseBody.create(artistResponseJson, MediaType.get("application/json"));
        doReturn(testResponseBody).when(responseMock).body();

        // Do
        var result = discogsApiClient.getArtistById(TEST_ARTIST_ID);

        // Assert and Verify
        assertEquals(new ArtistResponseDto(TEST_ARTIST_ID, TEST_ARTIST_NAME), result);
        verify(responseMock).isSuccessful();
        verify(callMock).execute();
        verifyNoMoreInteractions(callMock);
    }

    @Test
    void whenGetArtistByIdValidateNotFound_ThenThrowException() throws IOException {

        // When
        doReturn(false).when(responseMock).isSuccessful();
        doReturn("HTTP 404 Not Found").when(responseMock).toString();

        // Do
        assertThrows(DiscogsApiClientException.class, () -> discogsApiClient.getArtistById(TEST_ARTIST_ID));

        // Assert and Verify
        verify(responseMock).isSuccessful();
        verify(callMock).execute();
        verifyNoMoreInteractions(callMock);
    }

    @Test
    void whenGetArtistReleasesByIdValidateIsFound_ThenOk() throws IOException {

        // When
        doReturn(true).when(responseMock).isSuccessful();

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

        ResponseBody testResponseBody = ResponseBody.create(releaseResponseJson, MediaType.get("application/json"));
        doReturn(testResponseBody).when(responseMock).body();

        // Do
        var result = discogsApiClient.getReleasesByArtistId(TEST_ARTIST_ID);

        // Assert and Verify
        ApiResponseDto apiResponseDto = objectMapper.readValue(releaseResponseJson, ApiResponseDto.class);
        assertEquals(apiResponseDto, result);
        verify(responseMock).isSuccessful();
        verify(callMock).execute();
        verifyNoMoreInteractions(callMock);
    }

    @Test
    void whenGetReleasesByArtistIdValidateNotFound_ThenThrowException() throws IOException {

        // When
        doReturn(false).when(responseMock).isSuccessful();
        doReturn("HTTP 404 Not Found").when(responseMock).toString();

        // Do
        assertThrows(DiscogsApiClientException.class, () -> discogsApiClient.getReleasesByArtistId(TEST_ARTIST_ID));

        // Assert and Verify
        verify(responseMock).isSuccessful();
        verify(callMock).execute();
        verifyNoMoreInteractions(callMock);
    }
}