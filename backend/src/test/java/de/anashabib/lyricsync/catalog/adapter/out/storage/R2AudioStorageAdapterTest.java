package de.anashabib.lyricsync.catalog.adapter.out.storage;

import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Adobe's S3Mock speaks the same S3 API as R2, so CI tests the adapter without a Cloudflare account.
 * (MinIO was the first choice, but it no longer publishes container images.)
 */
@Testcontainers
class R2AudioStorageAdapterTest {

    @Container
    static GenericContainer<?> s3mock = new GenericContainer<>("adobe/s3mock:5.2.3")
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/").forPort(9090));

    private static S3Client s3;
    private static R2AudioStorageAdapter adapter;

    @BeforeAll
    static void bucket() {
        String endpoint = "http://" + s3mock.getHost() + ":" + s3mock.getMappedPort(9090);
        s3 = R2StorageConfig.s3Client(endpoint, "any-key", "any-secret"); // S3Mock doesn't check credentials
        s3.createBucket(b -> b.bucket("audio-test"));
        adapter = new R2AudioStorageAdapter(s3, "audio-test");
    }

    @Test
    void storesTheFileWithHeadersThatLetTheCdnKeepItForever() {
        byte[] audio = mp3("first");
        AudioKey key = AudioKey.ofMp3(audio);

        assertThat(adapter.store(key, audio)).isTrue();

        HeadObjectResponse head = s3.headObject(h -> h.bucket("audio-test").key(key.value()));
        assertThat(head.contentType()).isEqualTo("audio/mpeg");
        assertThat(head.cacheControl()).isEqualTo("public, max-age=31536000, immutable");
        assertThat(head.contentLength()).isEqualTo(audio.length);
    }

    @Test
    void storingTheSameFileAgainUploadsNothing() {
        byte[] audio = mp3("twice");
        AudioKey key = AudioKey.ofMp3(audio);

        adapter.store(key, audio);

        assertThat(adapter.store(key, audio)).isFalse();
    }

    @Test
    void servesPartsOfTheFileSoThePlayerCanSeek() {
        byte[] audio = mp3("0123456789");
        AudioKey key = AudioKey.ofMp3(audio);
        adapter.store(key, audio);

        ResponseBytes<GetObjectResponse> part =
                s3.getObjectAsBytes(g -> g.bucket("audio-test").key(key.value()).range("bytes=3-5"));

        assertThat(part.asUtf8String()).isEqualTo("012");
    }

    @Test
    void deletesAFileAndIgnoresOneThatIsAlreadyGone() {
        byte[] audio = mp3("gone");
        AudioKey key = AudioKey.ofMp3(audio);
        adapter.store(key, audio);

        adapter.delete(key);
        adapter.delete(key);

        assertThatThrownBy(() -> s3.headObject(h -> h.bucket("audio-test").key(key.value())))
                .isInstanceOf(NoSuchKeyException.class);
    }

    private static byte[] mp3(String content) {
        return ("ID3" + content).getBytes(StandardCharsets.UTF_8);
    }
}
