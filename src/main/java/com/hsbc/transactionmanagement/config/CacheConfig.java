package com.hsbc.transactionmanagement.config;

import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("transactions", "transaction");
        manager.setCacheSpecification("maximumSize=1000,expireAfterWrite=30m");
        return manager;
    }

}
