package com.renzo.green.controller;

import com.renzo.green.dto.RedisData;
import com.renzo.green.persistence.RedisDataPersistence;
import com.renzo.green.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/register")
    @Operation(
            summary = "Redis Key 등록",
            description = "Key 등록 기능을 제공합니다."
    )
    public ResponseEntity<?> registRedis(@RequestBody RedisData redisData) {
        redisService.registerData(RedisDataPersistence.builder()
                .key(redisData.getKey())
                .value(redisData.getValue())
                .build());
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }

    @GetMapping("/register/{key}")
    @Operation(
            summary = "Redis Key 조회",
            description = "Key 조회 기능을 제공합니다."
    )
    public ResponseEntity<?> getRedis(@PathVariable String key) {
        RedisDataPersistence redisDataVO = redisService.retrieveData(key);
        RedisData redisData = new RedisData();
        BeanUtils.copyProperties(redisDataVO, redisData);
        return ResponseEntity.status(HttpStatus.OK).body(redisData);
    }

}
