package de.anashabib.lyricsync.catalog.adapter.out.persistence.entity;

import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.LicenseType;
import de.anashabib.lyricsync.catalog.domain.model.SongStatus;
import de.anashabib.lyricsync.catalog.domain.model.SyncGranularity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "catalog", name = "song")
@Getter @Setter @NoArgsConstructor
public class SongEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    // Raw FK columns, not @ManyToOne — see explanation below.
    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @Column(name = "language_code", nullable = false)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;

    @Column(name = "duration_ms", nullable = false)
    private int durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    // optimistic locking: an UPDATE only succeeds if nobody changed the row since it was read,
    // and every successful UPDATE bumps it, which is what makes it usable as an ETag
    @Version
    private Long version;

    @Column(name = "license_type", nullable = false)
    private LicenseType licenseType; // converted by LicenseTypeConverter, autoApply

    @Column(name = "license_note", nullable = false)
    private String licenseNote;

    @Column(name = "audio_key", nullable = false)
    private String audioKey;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "sync_granularity", nullable = false)
    private SyncGranularity syncGranularity;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineIndex ASC")
    private List<SongLineEntity> lines = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}