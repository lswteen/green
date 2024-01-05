package com.renzo.green.service;

import com.renzo.green.persistence.RedisDataPersistence;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@SpringBootTest()
class RedisServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RedisDataPersistence redisDataPersistence;

    private void deleteByKey(String key){
        redisTemplate.delete(key);
    }

    @BeforeEach
    void setUp(){
        redisDataPersistence = RedisDataPersistence.builder()
            .key("green:renzo:code")
            .value("001")
            .build();
    }

    @Test
    void basic(){
        redisTemplate.opsForValue().set("green_key","green_val");
        Assertions.assertThat(redisTemplate.opsForValue().get("green_key").equals("green_key"));
        deleteByKey("green_key");
    }


    @Test
    void addItem(){
        redisTemplate.opsForValue().set(redisDataPersistence.getKey(),redisDataPersistence);
        Assertions.assertThat(redisTemplate.opsForValue().get(redisDataPersistence.getKey()).equals("redisTemplate"));
        deleteByKey(redisDataPersistence.getKey());
    }

}