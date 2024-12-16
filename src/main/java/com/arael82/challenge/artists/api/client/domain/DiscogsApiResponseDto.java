package com.arael82.challenge.artists.api.client.domain;

import java.util.List;

public record DiscogsApiResponseDto(
    DiscogsApiPaginationDto pagination,
    List<DiscogsApiReleaseResponseDto> releases
) {}