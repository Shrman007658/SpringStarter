package com.starter.api.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DefaultResponse {
    String status;
    UUID uuid;
}
