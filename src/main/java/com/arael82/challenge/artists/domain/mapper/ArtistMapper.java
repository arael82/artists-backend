package com.arael82.challenge.artists.domain.mapper;

import com.arael82.challenge.artists.data.model.Artist;
import com.arael82.challenge.artists.domain.ArtistResponseDto;

public class ArtistMapper {

    public static ArtistResponseDto toResponseDto(Artist updatedArtist) {
        return ArtistResponseDto.builder()
                .id(updatedArtist.getId())
                .apiId(updatedArtist.getApiId())
                .name(updatedArtist.getName())
                .albums(AlbumMapper.toResponseDtos(updatedArtist.getAlbums()))
                .createdOn(updatedArtist.getCreatedOn())
                .modifiedOn(updatedArtist.getModifiedOn())
                .build();
    }

}
