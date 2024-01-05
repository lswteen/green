package com.renzo.green.service;

import com.renzo.green.persistence.RedisDataPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void registerData(RedisDataPersistence persistence) {
        try{
            redisTemplate.opsForValue().set(persistence.getKey(), persistence);
        }catch (Exception e){

            log.error("### Redis Set Key Error !!! ::: {}", e.getMessage());
        }
    }

    public RedisDataPersistence retrieveData(String key) {
        RedisDataPersistence redisDataPersistence = null;
        try{
            redisDataPersistence = (RedisDataPersistence)redisTemplate.opsForValue().get(key);
        }catch (Exception e){

            log.error("### Redis Set Key Error !!! ::: {}", e.getMessage());
        }
        return redisDataPersistence;
    }

}
