package de.anashabib.lyricsync.catalog.config;

import de.anashabib.lyricsync.catalog.domain.encoding.LrcTextDecoder;
import de.anashabib.lyricsync.catalog.domain.parser.LrcParser;
import de.anashabib.lyricsync.catalog.domain.validation.LrcValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LrcProcessingConfig {

    @Bean
    public LrcParser lrcParser() {
        return new LrcParser();
    }

    @Bean
    public LrcValidator lrcValidator() {
        return new LrcValidator();
    }

    @Bean
    public LrcTextDecoder lrcTextDecoder() {
        return new LrcTextDecoder();
    }
}