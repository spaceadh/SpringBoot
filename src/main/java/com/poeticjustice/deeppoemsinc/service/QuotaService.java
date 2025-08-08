package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.models.mysql.UserQuota;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuotaService {
    private final RedisTemplate<String, Object> jedisRedisTemplate;
    private final RedisTemplate<String, Object> lettuceRedisTemplate;
    private static final long MAX_QUOTA = 1_000_000_000; // 1GB
    @Value("${spring.redis.client:jedis}")
    private String defaultClient;

    public QuotaService(
            @Lazy @Qualifier("jedisRedisTemplate") RedisTemplate<String, Object> jedisRedisTemplate,
            @Lazy @Qualifier("lettuceRedisTemplate") RedisTemplate<String, Object> lettuceRedisTemplate) {
        this.jedisRedisTemplate = jedisRedisTemplate;
        this.lettuceRedisTemplate = lettuceRedisTemplate;
    }

    public boolean hasEnoughQuota(String userId, long fileSize, String client) {
        UserQuota quota = getQuota(userId, client);
        return (quota.getUsedStorage() + fileSize) <= MAX_QUOTA;
    }

    public void updateQuota(String userId, long fileSize, String client) {
        UserQuota quota = getQuota(userId, client);
        quota.setUsedStorage(quota.getUsedStorage() + fileSize);
        getRedisTemplate(client).opsForValue().set("quota:" + userId, quota);
    }

    public UserQuota getQuota(String userId, String client) {
        RedisTemplate<String, Object> template = getRedisTemplate(client);
        UserQuota quota = (UserQuota) template.opsForValue().get("quota:" + userId);
        if (quota == null) {
            quota = new UserQuota();
            quota.setUserId(userId);
            quota.setUsedStorage(0);
            template.opsForValue().set("quota:" + userId, quota);
        }
        return quota;
    }

    public boolean isQuotaNearFull(String userId, String client) {
        UserQuota quota = getQuota(userId, client);
        return (quota.getUsedStorage() / (double) MAX_QUOTA) > 0.8;
    }

    private RedisTemplate<String, Object> getRedisTemplate(String client) {
        String selectedClient = (client != null && !client.isEmpty()) ? client : defaultClient;
        if (selectedClient.equalsIgnoreCase("jedis")) {
            return jedisRedisTemplate;
        } else if (selectedClient.equalsIgnoreCase("lettuce")) {
            return lettuceRedisTemplate;
        } else {
            throw new IllegalArgumentException("Invalid client type: " + selectedClient + ". Must be 'jedis' or 'lettuce'.");
        }
    }
}