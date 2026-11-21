package de.anashabib.content_ingestion.adapter.out.persistence.mapper;

import de.anashabib.content_ingestion.adapter.out.persistence.entity.ArtistEntity;
import de.anashabib.content_ingestion.domain.model.Artist;
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