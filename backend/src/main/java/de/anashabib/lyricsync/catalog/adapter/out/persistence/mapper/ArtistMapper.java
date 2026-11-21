package de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.entity.ArtistEntity;
import de.anashabib.lyricsync.catalog.domain.model.Artist;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public ArtistEntity toEntity(Artist artist) {
        ArtistEntity entity = new ArtistEntity();
        entity.setId(artist.id());
        entity.setName(artist.name());
        return entity;
    }

    public Artist toDomain(ArtistEntity entity) {
        return new Artist(entity.getId(), entity.getName());
    }
}