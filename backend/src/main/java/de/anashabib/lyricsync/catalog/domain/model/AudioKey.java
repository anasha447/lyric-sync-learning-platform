package de.anashabib.lyricsync.catalog.domain.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Where a song's audio lives in storage, relative to the storage root.
 * New uploads are named by their SHA-256, so the same file always gets the same key and
 * a changed file always gets a new one; that's what lets the CDN cache audio forever (ADR-0011).
 * Keys stored before that change are plain file names, which is why the format check only
 * makes sure a key can't point outside the storage root.
 */
public record AudioKey(String value) {
    private static final Pattern SAFE_RELATIVE_PATH = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._/-]*");

    public AudioKey {
        if (value == null || !SAFE_RELATIVE_PATH.matcher(value).matches() || value.contains("..")) {
            throw new IllegalArgumentException("Not a safe audio key: '" + value + "'");
        }
    }

    public static AudioKey ofMp3(byte[] content) {
        return new AudioKey("audio/" + HexFormat.of().formatHex(sha256(content)) + ".mp3");
    }

    private static byte[] sha256(byte[] content) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every JVM ships SHA-256", e);
        }
    }
}
