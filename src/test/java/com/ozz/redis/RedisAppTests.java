package com.ozz.redis;

import com.ozz.redis.direct.sentinel.JedisSentinelTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisAppTests {
	@Autowired
	RedisTemplate redisTemplate;
	@Autowired
	JedisSentinelTemplate jedisSentinelTemplate;

	@Test
	void contextLoads() {
		System.out.println(redisTemplate.opsForValue().get("myKey"));
		System.out.println(jedisSentinelTemplate.get("myKey"));
	}

}
