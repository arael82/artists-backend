package com.arael82.challenge.artists.controller;

import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.domain.AlbumResponseDto;
import com.arael82.challenge.artists.domain.ArtistComparisonResult;
import com.arael82.challenge.artists.domain.ArtistResponseDto;
import com.arael82.challenge.artists.domain.MultiArtistComparison;
import com.arael82.challenge.artists.service.ArtistService;
import com.arael82.challenge.artists.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class ArtistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtistService artistService;

    @BeforeEach
    void setup() {
        var controller = new ArtistController(artistService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    @DisplayName("GET /artists/{artistId} - success")
    void getArtist_success() throws Exception {
        ArtistResponseDto mockArtist = new ArtistResponseDto();
        mockArtist.setApiId(123L);
        mockArtist.setName("Test Artist");

        when(artistService.retrieveArtist(123L)).thenReturn(mockArtist);

        mockMvc.perform(get("/artists/{artistId}", 123L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.apiId").value(123L))
                .andExpect(jsonPath("$.name").value("Test Artist"));
    }

    @Test
    @DisplayName("GET /artists/{artistId} - not found scenario (404)")
    void getArtist_notFound() throws Exception {
        Mockito.doThrow(new NotFoundException("Not found")).when(artistService).retrieveArtist(999L);

        mockMvc.perform(get("/artists/{artistId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /artists/{artistId}/albums - success with pagination")
    void getAlbumsByArtistId_success() throws Exception {
        AlbumResponseDto albumDto1 = new AlbumResponseDto();
        albumDto1.setApiId(101L);
        albumDto1.setTitle("Thriller");

        AlbumResponseDto albumDto2 = new AlbumResponseDto();
        albumDto2.setApiId(102L);
        albumDto2.setTitle("Shape of You");

        List<AlbumResponseDto> dtos = List.of(albumDto1, albumDto2);
        Page<AlbumResponseDto> pageResult = new PageImpl<>(dtos, PageRequest.of(0, 2), 2);

        when(artistService.searchAlbums(
                eq(123L),      // artistId
                eq("Rock"),    // genre
                eq("thr"),     // title
                eq(1982),      // year
                eq(1),         // page
                eq(20)         // size
        )).thenReturn(pageResult);

        mockMvc.perform(get("/artists/{artistId}/albums", 123L)
                        .param("genre", "Rock")
                        .param("title", "thr")
                        .param("year", "1982")
                        .param("page", "1")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].apiId").value(101L))
                .andExpect(jsonPath("$.content[0].title").value("Thriller"))
                .andExpect(jsonPath("$.content[1].apiId").value(102L))
                .andExpect(jsonPath("$.content[1].title").value("Shape of You"));
    }

    @Test
    @DisplayName("GET /artists/{artistId}/albums - empty result")
    void getAlbumsByArtistId_empty() throws Exception {
        Page<AlbumResponseDto> emptyPage = new PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(artistService.searchAlbums(
                eq(999L), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/artists/{artistId}/albums", 999L)
                        .param("page", "1")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /artists/compare - success with multiple artists")
    void compareArtists_success() throws Exception {

        List<ArtistComparisonResult> comparisons = List.of(
                new ArtistComparisonResult(new Artist(1001L, "Artist 1")),
                new ArtistComparisonResult(new Artist(1002L, "Artist 2"))
        );

        MultiArtistComparison comparison = new MultiArtistComparison(comparisons);

        when(artistService.compareArtists(List.of(1001L, 1002L)))
                .thenReturn(comparison);

        mockMvc.perform(get("/artists/compare")
                        .param("artistIds", "1001", "1002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isArray());
    }

    @Test
    @DisplayName("GET /artists/compare - no artists provided (400?)")
    void compareArtists_noArtistIds() throws Exception {
        mockMvc.perform(get("/artists/compare"))
                .andExpect(status().isBadRequest());
    }
}
