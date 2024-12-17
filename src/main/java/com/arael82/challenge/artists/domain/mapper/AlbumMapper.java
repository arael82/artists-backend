package com.arael82.challenge.artists.domain.mapper;

import com.arael82.challenge.artists.data.model.Album;
import com.arael82.challenge.artists.domain.AlbumResponseDto;

import java.util.List;
import java.util.stream.Collectors;

public class AlbumMapper {

    public static AlbumResponseDto toResponseDto(Album source) {
        return AlbumResponseDto.builder()
                .id(source.getId())
                .apiId(source.getApiId())
                .genre(source.getGenre())
                .title(source.getTitle())
                .releaseYear(source.getReleaseYear())
                .build();
    }

    public static List<AlbumResponseDto> toResponseDtos(List<Album> source) {
        return source.stream()
                .map(AlbumMapper::toResponseDto)
                .collect(Collectors.toList());
    }

}
