package de.anashabib.lyricsync.catalog.domain.model;

import java.util.Arrays;
import java.util.stream.Collectors;

/** The licenses that allow translation and commercial use (ADR-0008). NC and ND aren't here on purpose. */
public enum LicenseType {
    PUBLIC_DOMAIN("public_domain"),  // also CC0
    CC_BY("cc_by"),
    CC_BY_SA("cc_by_sa"),
    OWN_RECORDING("own_recording"),
    WRITTEN_PERMISSION("written_permission");

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
        throw new IllegalArgumentException("Unknown license type '" + dbValue + "', expected one of " + allowed());
    }

    private static String allowed() {
        return Arrays.stream(values()).map(LicenseType::dbValue).collect(Collectors.joining(", "));
    }
}
