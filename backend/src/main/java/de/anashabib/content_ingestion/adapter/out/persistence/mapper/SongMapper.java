package de.anashabib.content_ingestion.adapter.out.persistence.mapper;

import de.anashabib.content_ingestion.adapter.out.persistence.entity.SongEntity;
import de.anashabib.content_ingestion.adapter.out.persistence.entity.SongLineEntity;
import de.anashabib.content_ingestion.domain.model.Song;
import de.anashabib.content_ingestion.domain.model.SongLine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SongMapper {

    public SongEntity toEntity(Song song) {
        SongEntity entity = new SongEntity();
        entity.setId(song.id());
        entity.setTitle(song.title());
        entity.setArtistId(song.artistId());
        entity.setLanguageCode(song.languageCode());
        entity.setCefrLevel(song.cefrLevel());
        entity.setDurationMs(song.durationMs());
        entity.setLicenseType(song.licenseType());
        entity.setAudioSourceRef(song.audioSourceRef());
        entity.setCoverImageUrl(song.coverImageUrl());
        entity.setSyncGranularity(song.syncGranularity());

        List<SongLineEntity> lineEntities = song.lines().stream()
                .map(line -> toLineEntity(line, entity))
                .toList();

        // Mutate the managed collection in place — see explanation below.
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    private SongLineEntity toLineEntity(SongLine line, SongEntity parent) {
        SongLineEntity entity = new SongLineEntity();
        entity.setId(line.id());
        entity.setSong(parent); // required — see the mapper test below
        entity.setLineIndex(line.lineIndex());
        entity.setRawText(line.rawText());
        entity.setStartMs(line.startMs());
        entity.setEndMs(line.endMs());
        entity.setInstrumental(line.instrumental());
        entity.setContextualTranslation(line.contextualTranslation());
        entity.setCulturalNote(line.culturalNote());
        return entity;
    }

    public Song toDomain(SongEntity entity) {
        List<SongLine> lines = entity.getLines().stream()
                .map(this::toDomainLine)
                .toList();

        return new Song(
                entity.getId(), entity.getTitle(), entity.getArtistId(), entity.getLanguageCode(),
                entity.getCefrLevel(), entity.getDurationMs(), entity.getLicenseType(),
                entity.getAudioSourceRef(), entity.getCoverImageUrl(), entity.getSyncGranularity(),
                lines, entity.getCreatedAt()
        );
    }

    private SongLine toDomainLine(SongLineEntity entity) {
        return new SongLine(
                entity.getId(), entity.getLineIndex(), entity.getRawText(),
                entity.getStartMs(), entity.getEndMs(), entity.isInstrumental(),
                entity.getContextualTranslation(), entity.getCulturalNote()
        );
    }
}