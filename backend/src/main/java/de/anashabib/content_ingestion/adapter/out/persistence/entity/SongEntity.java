package de.anashabib.content_ingestion.adapter.out.persistence.entity;

import de.anashabib.content_ingestion.domain.model.LicenseType;
import de.anashabib.content_ingestion.domain.model.SyncGranularity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "songs")
@Getter @Setter @NoArgsConstructor
public class SongEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    // Raw FK columns, not @ManyToOne — see explanation below.
    @Column(name = "artist_id")
    private UUID artistId;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "cefr_level")
    private String cefrLevel;

    @Column(name = "duration_ms", nullable = false)
    private int durationMs;

    @Column(name = "license_type", nullable = false)
    private LicenseType licenseType; // converted by LicenseTypeConverter, autoApply

    @Column(name = "audio_source_ref", nullable = false)
    private String audioSourceRef;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "sync_granularity", nullable = false)
    private SyncGranularity syncGranularity;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineIndex ASC")
    private List<SongLineEntity> lines = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}