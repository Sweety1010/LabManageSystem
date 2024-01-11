package com.lab.labmanagesystem.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
public class AliOssProperties {

    @Value("${aliOss.endpoint}")
    private String endpoint;

    @Value("${aliOss.access-key-id}")
    private String accessKeyId;

    @Value("${aliOss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliOss.bucket-name}")
    private String bucketName;

}
