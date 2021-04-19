package com.ozz.redis.config;

import com.ozz.redis.direct.sentinel.JedisSentinelTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty("spring.redis.sentinel.nodes")
public class MyRedisSentinelConfig {
  @Bean
  public JedisSentinelTemplate jedisSentinelTemplate() {
    return new JedisSentinelTemplate();
  }
}