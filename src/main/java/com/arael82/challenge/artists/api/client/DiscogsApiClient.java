package com.arael82.challenge.artists.api.client;

import com.arael82.challenge.artists.api.client.domain.ApiResponseDto;
import com.arael82.challenge.artists.api.client.domain.ArtistResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class DiscogsApiClient {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final String baseUrl;

    private final String apiToken;

    private final OkHttpClient httpClient;

    private final ObjectMapper objectMapper;

    @Autowired
    public DiscogsApiClient(
            @Value("${discogs.api.base-url}") String baseUrl,
            @Value("${discogs.api.token}") String apiToken,
            ObjectMapper objectMapper
    ) {
        this.httpClient = new OkHttpClient();
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieve artist information by ID.
     *
     * @param artistId the ID of the artist to retrieve.
     * @return JSON response as a string.
     * @throws IOException if the request fails.
     */
    @SuppressWarnings("DataFlowIssue")
    public ArtistResponseDto getArtistById(Integer artistId) throws IOException {

        String url = String.format("%s/artists/%d", baseUrl, artistId);

        log.info("Requesting artist with ID: {} on Discogs API.", artistId);

        try (Response response = httpClient.newCall(buildRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                throw new DiscogsApiClientException("Unexpected code: " + response);
            }
            String responseBody = response.body().string();

            return objectMapper.readValue(responseBody, ArtistResponseDto.class);
        }
    }

    /**
     * Retrieve artist information by ID.
     *
     * @param artistId the ID of the artist to retrieve.
     * @return JSON response as a string.
     * @throws IOException if the request fails.
     */
    @SuppressWarnings("DataFlowIssue")
    public ApiResponseDto getReleasesByArtistId(Integer artistId) throws IOException {

        String url = String.format("%s/artists/%d/releases", baseUrl, artistId);

        log.info("Requesting releases for artist with ID: {} on Discogs API.", artistId);

        try (Response response = httpClient.newCall(buildRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                throw new DiscogsApiClientException("Unexpected code: " + response);
            }
            String responseBody = response.body().string();

            return objectMapper.readValue(responseBody, ApiResponseDto.class);
        }
    }

    /**
     * Build a request with the given URL.
     * @param url the URL to build the request with.
     * @return the built request.
     */
    @NotNull
    private Request buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader(AUTHORIZATION_HEADER, "Discogs token=" + apiToken)
                .build();
    }

}
