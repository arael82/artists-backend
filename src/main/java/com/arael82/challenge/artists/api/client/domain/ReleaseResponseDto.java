package com.arael82.challenge.artists.api.client.domain;

public record ReleaseResponseDto(
        Long id,
        String status,
        String type,
        String format,
        String label,
        String title,
        String role,
        String artist,
        Integer year,
        String thumb
) {
}
