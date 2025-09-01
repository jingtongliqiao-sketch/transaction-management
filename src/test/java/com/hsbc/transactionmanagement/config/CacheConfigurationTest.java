package com.hsbc.transactionmanagement.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
class CacheConfigurationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assert(applicationContext != null);
        System.out.println("Context loaded successfully");
    }

    @Test
    void checkCacheConfig() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.stream(beanNames)
                .filter(name -> name.contains("cache") || name.contains("transaction"))
                .forEach(System.out::println);
    }
}