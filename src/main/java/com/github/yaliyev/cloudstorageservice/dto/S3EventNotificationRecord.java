package com.github.yaliyev.cloudstorageservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3EventNotificationRecord(
        @JsonProperty("eventName") String eventName,
        @JsonProperty("s3") S3Entity s3
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record S3Entity(
            @JsonProperty("bucket") BucketEntity bucket,
            @JsonProperty("object") ObjectEntity object
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BucketEntity(
            @JsonProperty("name") String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjectEntity(
            @JsonProperty("key") String key,
            @JsonProperty("size") Long size
    ) {}
}
