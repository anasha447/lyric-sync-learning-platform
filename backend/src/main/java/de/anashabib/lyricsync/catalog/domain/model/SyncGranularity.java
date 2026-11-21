package de.anashabib.lyricsync.catalog.domain.model;

public enum SyncGranularity {
    LINE("line"),
    WORD("word");

    private final String dbValue;

    SyncGranularity(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static SyncGranularity fromDbValue(String dbValue) {
        for (SyncGranularity value : values()) {
            if (value.dbValue.equals(dbValue)) return value;
        }
        throw new IllegalArgumentException("Unknown sync_granularity value: " + dbValue);
    }
}