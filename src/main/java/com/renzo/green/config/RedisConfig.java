package com.renzo.green.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisCacheProperties cacheProperties;
    private final RedisProperties redisProperties;

    /**
     * clientConfigurationBuilder 확장 구현을 위한 method
     *
     * @param clientConfigurationBuilder LettuceClientConfigurationBuilder
     * @param cacheProperties            RedisCacheProperties
     */
    void externalConfigurationClientConfigurationBuilder(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigurationBuilder,
            RedisCacheProperties cacheProperties) {
        log.debug("MasterReplica Type ReadFrom : {}", cacheProperties.getReplica().getReadFrom());
        clientConfigurationBuilder.readFrom(cacheProperties.getReplica().getReadFrom());
    }

    /**
     * LettuceClientConfigurationBuilder 에 대한 Customizer 를 생성하여 적용합니다.
     *
     * @param cacheProperties RedisCacheProperties
     * @return LettuceClientConfigurationBuilderCustomizer
     */
    protected LettuceClientConfigurationBuilderCustomizer createLettuceClientConfigurationBuilderCustomizer(
            RedisCacheProperties cacheProperties) {
        return clientConfigurationBuilder -> {
            if (clientConfigurationBuilder.build().isUseSsl() &&
                    Boolean.TRUE.equals(cacheProperties.getDisablePeerVerification())) {
                clientConfigurationBuilder.useSsl().disablePeerVerification();
            }
            externalConfigurationClientConfigurationBuilder(clientConfigurationBuilder, cacheProperties);
        };
    }

    protected LettuceClientConfiguration.LettuceClientConfigurationBuilder createPoolingConfigurationBuilder(
            RedisCacheProperties.Pool poolProperties) {
        log.info("LettuceClientConfigurationBuilder - Pooling Configuration Builder");
        var poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(poolProperties.getMaxIdle());
        poolConfig.setMinIdle(poolProperties.getMinIdle());
        poolConfig.setMaxTotal(poolProperties.getMaxActive());
        poolConfig.setMaxWait(Duration.ofMillis(poolProperties.getMaxWait()));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(poolProperties.getTimeBetweenEvictionRuns()));
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig);
    }

    protected boolean isPoolEnable(RedisCacheProperties.Pool poolProperties) {
        return poolProperties != null && poolProperties.getEnabled();
    }

    protected LettuceClientConfiguration.LettuceClientConfigurationBuilder createDefaultClientConfigurationBuilder() {
        log.info("LettuceClientConfigurationBuilder - Default Configuration Builder");
        return LettuceClientConfiguration.builder();
    }

    protected LettuceClientConfiguration createLettuceClientConfiguration(RedisCacheProperties cacheProperties) {
        var builder = isPoolEnable(cacheProperties.getPool()) ?
                createPoolingConfigurationBuilder(cacheProperties.getPool()) :
                createDefaultClientConfigurationBuilder();

        if (Boolean.TRUE.equals(cacheProperties.getSsl().getEnabled())) {
            builder.useSsl();
        }
        createLettuceClientConfigurationBuilderCustomizer(cacheProperties).customize(builder);
        builder.clientOptions(new ClientOptionFactory(cacheProperties.getClientOptions()).create());
        return builder.build();
    }


    // lettuce 사용시
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        log.debug("RedisConnectionFactory Type : MasterReplica");
        var properties = redisProperties;
        var masterReplicaConfiguration
                = new RedisStaticMasterReplicaConfiguration(properties.getHost(), properties.getPort());
        masterReplicaConfiguration.setUsername(properties.getUsername());
        masterReplicaConfiguration.setPassword(properties.getPassword());

        cacheProperties.getReplica().getNodes().forEach(node -> masterReplicaConfiguration
                .addNode(node.getHost(), node.getPort()));

        var result = new LettuceConnectionFactory(masterReplicaConfiguration,
                createLettuceClientConfiguration(cacheProperties));
        result.setEagerInitialization(cacheProperties.getEagerInitialization());
        return result;
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory){
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(CacheKey.DEFAULT_EXPIRE_SEC))
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))

                .serializeValuesWith(RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));


        // 캐시키 별 default 유효시간 설정
        Map<String, RedisCacheConfiguration> cacheConfiguration = new HashMap<>();
        cacheConfiguration.put(CacheKey.ZONE,RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(CacheKey.ZONE_EXPIRE_SEC)));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(configuration)
                .withInitialCacheConfigurations(cacheConfiguration)
                .build();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer() );

        return redisTemplate;
    }

}
