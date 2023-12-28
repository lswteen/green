package com.renzo.green.persistence;

import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisDataPersistence {
    private String key;
    private String value;
}
