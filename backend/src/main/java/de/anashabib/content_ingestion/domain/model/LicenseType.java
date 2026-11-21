package de.anashabib.content_ingestion.domain.model;

public enum LicenseType {
    PUBLIC_DOMAIN("public_domain"),
    CC_BY("cc_by"),
    ORIGINAL("original"),
    LICENSED("licensed");

    private final String dbValue;

    LicenseType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static LicenseType fromDbValue(String dbValue) {
        for (LicenseType type : values()) {
            if (type.dbValue.equals(dbValue)) return type;
        }
        throw new IllegalArgumentException("Unknown license_type value: " + dbValue);
    }
}