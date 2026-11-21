package de.anashabib.content_ingestion.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "song_lines")
@Getter @Setter @NoArgsConstructor
public class SongLineEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private SongEntity song;

    @Column(name = "line_index", nullable = false)
    private int lineIndex;

    @Column(name = "raw_text", nullable = false)
    private String rawText;

    @Column(name = "start_ms", nullable = false)
    private int startMs;

    @Column(name = "end_ms", nullable = false)
    private int endMs;

    @Column(name = "is_instrumental", nullable = false)
    private boolean instrumental;

    @Column(name = "contextual_translation")
    private String contextualTranslation;

    @Column(name = "cultural_note")
    private String culturalNote;
}