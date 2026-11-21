package de.anashabib.content_ingestion.config;

import de.anashabib.content_ingestion.domain.encoding.LrcTextDecoder;
import de.anashabib.content_ingestion.domain.parser.LrcParser;
import de.anashabib.content_ingestion.domain.validation.LrcValidator;
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