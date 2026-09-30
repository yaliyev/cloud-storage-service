package com.github.yaliyev.cloudstorageservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3EventNotification(
        @JsonProperty("Records") List<S3EventNotificationRecord> records
) {}
