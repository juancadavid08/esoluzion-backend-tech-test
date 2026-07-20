package com.techtest.similarproducts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class SimilarProductsExecutorConfig {

    @Bean(name = "similarProductsExecutor")
    public Executor similarProductsExecutor(
            @Value("${similar-products.pool-size:16}") int poolSize,
            @Value("${similar-products.queue-capacity:100}") int queueCapacity) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("similar-products-");
        executor.initialize();
        return executor;
    }
}
