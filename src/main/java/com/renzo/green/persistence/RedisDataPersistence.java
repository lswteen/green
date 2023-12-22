package com.renzo.green.persistence;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class RedisDataPersistence {
    private String key;
    private String value;
}
