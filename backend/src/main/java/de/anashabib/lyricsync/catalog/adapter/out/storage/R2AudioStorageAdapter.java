package de.anashabib.lyricsync.catalog.adapter.out.storage;

import de.anashabib.lyricsync.catalog.application.port.out.AudioStorageException;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStoragePort;
import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Audio in Cloudflare R2, through R2's S3-compatible API. Browsers fetch it from the bucket's
 * public domain behind Cloudflare's CDN, never through the backend (ADR-0005, ADR-0011).
 */
public class R2AudioStorageAdapter implements AudioStoragePort {

    // the key is the file's SHA-256: the bytes behind a URL never change, so caches may keep them forever
    static final String CACHE_FOREVER = "public, max-age=31536000, immutable";

    private final S3Client s3;
    private final String bucket;

    public R2AudioStorageAdapter(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public boolean store(AudioKey key, byte[] audioBytes) {
        try {
            if (exists(key)) {
                return false; // same hash, same bytes: already there
            }
            s3.putObject(put -> put.bucket(bucket).key(key.value())
                            .contentType("audio/mpeg")
                            .cacheControl(CACHE_FOREVER),
                    RequestBody.fromBytes(audioBytes));
            return true;
        } catch (SdkException e) {
            throw new AudioStorageException("Failed to store audio in R2: " + key.value(), e);
        }
    }

    @Override
    public void delete(AudioKey key) {
        try {
            s3.deleteObject(del -> del.bucket(bucket).key(key.value())); // a missing key counts as success
        } catch (SdkException e) {
            throw new AudioStorageException("Failed to delete audio from R2: " + key.value(), e);
        }
    }

    private boolean exists(AudioKey key) {
        try {
            s3.headObject(head -> head.bucket(bucket).key(key.value()));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
