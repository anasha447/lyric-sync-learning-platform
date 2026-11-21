package de.anashabib.content_ingestion.adapter.out.persistence.converter;

import de.anashabib.content_ingestion.domain.model.LicenseType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LicenseTypeConverter implements AttributeConverter<LicenseType, String> {

    @Override
    public String convertToDatabaseColumn(LicenseType attribute) {
        return attribute == null ? null : attribute.dbValue();
    }

    @Override
    public LicenseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LicenseType.fromDbValue(dbData);
    }
}