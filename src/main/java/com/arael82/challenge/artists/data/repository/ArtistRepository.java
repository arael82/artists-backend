package com.arael82.challenge.artists.data.repository;

import com.arael82.challenge.artists.data.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByApiId(Long apiId);

}
