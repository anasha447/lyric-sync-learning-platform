package de.anashabib.lyricsync.catalog.adapter.out.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/** Active with {@code app.storage.type=r2}; otherwise the local filesystem adapter is used. */
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
public class R2StorageConfig {

    @Bean
    S3Client r2Client(@Value("${app.storage.r2.endpoint}") String endpoint,
                      @Value("${app.storage.r2.access-key-id}") String accessKeyId,
                      @Value("${app.storage.r2.secret-access-key}") String secretAccessKey) {
        return s3Client(endpoint, accessKeyId, secretAccessKey);
    }

    @Bean
    R2AudioStorageAdapter r2AudioStorageAdapter(S3Client r2Client,
                                                @Value("${app.storage.r2.bucket}") String bucket) {
        return new R2AudioStorageAdapter(r2Client, bucket);
    }

    static S3Client s3Client(String endpoint, String accessKeyId, String secretAccessKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))       // https://<account id>.r2.cloudflarestorage.com
                .region(Region.of("auto"))                   // R2 has no regions; "auto" is what it expects
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .forcePathStyle(true)                        // bucket in the path: works for R2 and MinIO alike
                // newer SDKs send extra checksums by default that R2 doesn't accept; only send required ones
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }
}
