package com.arael82.challenge.artists.api.client.domain;

import java.util.List;

public record ApiResponseDto(
    Pagination pagination,
    List<ReleaseResponseDto> releases
) {}