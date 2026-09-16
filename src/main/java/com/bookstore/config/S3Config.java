package com.bookstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 configuration.
 *
 * <p>
 * Creates the AWS S3 client and S3 presigner used by the
 * application's storage layer.
 * </p>
 *
 * <p>
 * AWS credentials are resolved using the AWS SDK default
 * credentials provider chain. Credentials are therefore not
 * hardcoded in the application.
 * </p>
 */
@Configuration
public class S3Config {

    /**
     * Creates the Amazon S3 client.
     *
     * @param awsRegion AWS region in which the S3 bucket is located
     * @return configured S3 client
     */
    @Bean
    public S3Client s3Client(
            @Value("${aws.region}") String awsRegion) {

        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(
                        DefaultCredentialsProvider.create()
                )
                .build();
    }

    /**
     * Creates the Amazon S3 presigner.
     *
     * <p>
     * The presigner generates temporary signed URLs that allow
     * authorized clients to access private S3 objects.
     * </p>
     *
     * @param awsRegion AWS region in which the S3 bucket is located
     * @return configured S3 presigner
     */
    @Bean
    public S3Presigner s3Presigner(
            @Value("${aws.region}") String awsRegion) {

        return S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(
                        DefaultCredentialsProvider.create()
                )
                .build();
    }
}