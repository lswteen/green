package com.renzo.green.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class RedisData {
    private String key;
    private String value;
}
