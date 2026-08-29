package com.bookstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Provides the AWS S3 client used by the application for object-storage
 * operations such as book-cover and other media file management.
 *
 * <p>The client uses AWS SDK for Java 2.x and the default AWS credential
 * provider chain. Credentials are therefore resolved from the standard
 * AWS-supported external sources instead of being hardcoded in the
 * application.</p>
 */
@Configuration
public class S3Config {

    /**
     * Creates the shared AWS S3 client used by the application.
     *
     * @param awsRegion AWS region configured for the application
     * @return configured S3 client
     */
    @Bean
    public S3Client s3Client(
            @Value("${aws.region}") String awsRegion) {

        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}