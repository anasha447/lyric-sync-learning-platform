package de.anashabib.lyricsync.catalog.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.time.Duration;

/**
 * In development there's no R2, so the backend serves the stored audio itself under /media/**,
 * with the same headers the CDN uses. Spring's resource handler answers range requests (206),
 * so seeking works locally exactly as it will from R2.
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalMediaConfig implements WebMvcConfigurer {

    private final Path audioRoot;

    public LocalMediaConfig(@Value("${app.storage.audio-root}") String audioRoot) {
        this.audioRoot = Path.of(audioRoot).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations(audioRoot.toUri().toString())
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
    }
}
