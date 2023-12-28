package com.renzo.green.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CacheKey {
    public static final int DEFAULT_EXPIRE_SEC = 60; // 1 minutes
    public static final String ZONE = "zone";
    public static final int ZONE_EXPIRE_SEC = 30; // 30 sec
}
