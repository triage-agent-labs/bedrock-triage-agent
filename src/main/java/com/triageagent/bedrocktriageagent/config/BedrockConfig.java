package com.triageagent.bedrocktriageagent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;

@Configuration
public class BedrockConfig {

    @Bean
    BedrockRuntimeClient bedrockRuntimeClient(
            @Value("${app.aws.region}") String region,
            @Value("${app.aws.profile:}") String profile // optional
    ) {

        // Use an explicit profile when provided (e.g., SSO), otherwise fall back to the default chain.
        AwsCredentialsProvider credentials =
                (profile != null && !profile.isBlank())
                        ? ProfileCredentialsProvider.builder().profileName(profile).build()
                        : DefaultCredentialsProvider.create();

        return BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .httpClientBuilder(
                        ApacheHttpClient.builder()
                                .maxConnections(50)
                                .connectionTimeout(Duration.ofSeconds(5))
                                .socketTimeout(Duration.ofSeconds(30))
                )
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .apiCallAttemptTimeout(Duration.ofSeconds(30))
                                .apiCallTimeout(Duration.ofSeconds(60))
                                .build()
                )
                .build();
    }
}
