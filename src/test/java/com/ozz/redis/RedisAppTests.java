package com.ozz.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisAppTests {
	@Autowired
	RedisTemplate redisTemplate;

	@Test
	void contextLoads() {
		System.out.println(redisTemplate.opsForValue().get("myKey"));
	}

}
