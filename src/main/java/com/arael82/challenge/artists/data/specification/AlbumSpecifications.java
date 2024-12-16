package com.arael82.challenge.artists.data.specification;

import com.arael82.challenge.artists.data.model.Album;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class AlbumSpecifications {

    /**
     * Filter by "active" status on the Album entity.
     */
    public static Specification<Album> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return cb.conjunction(); 
            return cb.equal(root.get("active"), active);
        };
    }

    /**
     * Filter by the album's genre (exact match).
     */
    public static Specification<Album> hasGenre(String genre) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(genre)) return cb.conjunction();
            return cb.equal(root.get("genre"), genre);
        };
    }

    /**
     * Filter by the album's title (case-insensitive partial match).
     */
    public static Specification<Album> titleContains(String title) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(title)) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    /**
     * Filter albums by release year (exact match).
     */
    public static Specification<Album> hasYear(Integer year) {
        return (root, query, cb) -> {
            if (year == null) return cb.conjunction();
            return cb.equal(root.get("year"), year);
        };
    }

    /**
     * Filter by the album's associated Artist ID (join).
     */
    public static Specification<Album> hasArtistApiId(Long artistApiId) {
        return (root, query, cb) -> {
            if (artistApiId == null) return cb.conjunction();
            // We join the 'artist' relationship to filter by Artist.apiId
            return cb.equal(root.get("artist").get("apiId"), artistApiId);
        };
    }

}
