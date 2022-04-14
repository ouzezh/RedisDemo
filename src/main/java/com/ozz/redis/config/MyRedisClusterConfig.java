package com.ozz.redis.config;

import cn.hutool.log.StaticLog;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty("spring.custom.redis.cluster.nodes")
public class MyRedisClusterConfig {
  @Bean
  @Primary
  public RedisTemplate redisTemplate(@Value("${spring.custom.redis.cluster.nodes}") String nodes,
      @Value("${spring.custom.redis.cluster.password}") String password,
      @Value("${spring.custom.redis.cluster.max-redirects}") int maxRedirect,
      @Value("${spring.custom.redis.cluster.timeout}") Duration timeout,
      @Value("${spring.custom.redis.cluster.max-active}") int maxActive,
      @Value("${spring.custom.redis.cluster.max-wait}") int maxWait,
      @Value("${spring.custom.redis.cluster.max-idle}") int maxIdle,
      @Value("${spring.custom.redis.cluster.min-idle}") int minIdle) {
    StaticLog.debug("--> start init " + this.getClass().getName());
    JedisConnectionFactory connectionFactory =  jedisClusterConnectionFactory(nodes, password,
        timeout, maxRedirect, maxActive, maxWait, maxIdle, minIdle);
    return createRedisClusterTemplate(connectionFactory);
  }

  private JedisConnectionFactory jedisClusterConnectionFactory(String nodes, String password,
      Duration timeout, int maxRedirect, int maxActive, int maxWait, int maxIdle, int minIdle) {
    RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
    List<RedisNode> nodeList = Arrays
        .stream(nodes.replaceAll("\\s", "").split(","))
        .map(node -> new RedisNode(node.split(":")[0], Integer.valueOf(node.split(":")[1])))
        .collect(Collectors.toList());
    redisClusterConfiguration.setClusterNodes(nodeList);
    redisClusterConfiguration.setPassword(password);
    redisClusterConfiguration.setMaxRedirects(maxRedirect);

    // 连接池通用配置
    GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
    genericObjectPoolConfig.setMaxIdle(maxIdle);
    genericObjectPoolConfig.setMaxTotal(maxActive);
    genericObjectPoolConfig.setMinIdle(minIdle);
    genericObjectPoolConfig.setMaxWaitMillis(maxWait);
    genericObjectPoolConfig.setTestWhileIdle(true);
    genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(300000);

    JedisClientConfiguration.DefaultJedisClientConfigurationBuilder builder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration
        .builder();
    builder.connectTimeout(timeout);
    builder.usePooling();
    builder.poolConfig(genericObjectPoolConfig);
    JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisClusterConfiguration, builder.build());
    // 连接池初始化
    connectionFactory.afterPropertiesSet();

    return connectionFactory;
  }

  private RedisTemplate createRedisClusterTemplate(JedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    FastJsonRedisSerializer redisSerializer = new FastJsonRedisSerializer(Object.class);

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    // key采用String的序列化方式
    redisTemplate.setKeySerializer(stringRedisSerializer);
    // hash的key也采用String的序列化方式
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    // value序列化方式采用jackson
    redisTemplate.setValueSerializer(redisSerializer);
    // hash的value序列化方式采用jackson
    redisTemplate.setHashValueSerializer(redisSerializer);
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }
}