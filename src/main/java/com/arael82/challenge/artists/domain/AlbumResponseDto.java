package com.arael82.challenge.artists.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDto {

    private Long id;

    private Long apiId;

    private String genre;

    private String title;

    private Integer releaseYear;

}
