package com.heima.common.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 */
@Configuration
@EnableConfigurationProperties(MinIOProperties.class)
public class MinIOConfiguration {
    @Autowired
    private MinIOProperties minIOProperties;

    @Bean
    public MinioClient minioClient(){
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minIOProperties.getEndpoint())
                .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                .build();
        return minioClient;
    }
}
