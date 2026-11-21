package de.anashabib.lyricsync.catalog.adapter.out.persistence.converter;

import de.anashabib.lyricsync.catalog.domain.model.SyncGranularity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SyncGranularityConverter implements AttributeConverter<SyncGranularity, String> {

    @Override
    public String convertToDatabaseColumn(SyncGranularity attribute) {
        return attribute == null ? null : attribute.dbValue();
    }

    @Override
    public SyncGranularity convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SyncGranularity.fromDbValue(dbData);
    }
}