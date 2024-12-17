package com.arael82.challenge.artists.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "albums")
@Getter
@Setter
@NoArgsConstructor
public class Album extends AbstractEntity {

    public Album(Artist artist, Long apiId, String title, String genre, Integer year) {
        this.artist = artist;
        this.apiId = apiId;
        this.genre = genre;
        this.title = title;
        this.releaseYear = year;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_id", nullable = false, unique = true)
    private Long apiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    @ToString.Exclude
    private Artist artist;

    @Column(name = "genre_id")
    private String genre;

    @Column(nullable = false)
    private String title;

    @Column
    private Integer releaseYear;

}
