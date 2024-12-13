package com.arael82.challenge.artists.api.client.domain;

public record DiscogsApiPaginationDto(
    int page,
    int pages,
    int per_page,
    int items
) {}

