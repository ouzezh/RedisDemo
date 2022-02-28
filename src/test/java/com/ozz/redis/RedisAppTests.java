package com.ozz.redis;

import com.ozz.redis.service.MyRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

@SpringBootTest
class RedisAppTests {
    @Autowired
    MyRedisService myRedisService;

    @Test
    void contextLoads() {
        System.out.println(myRedisService.optimisticSet("myKey", "1", Duration.ofHours(1), "1"));
    }

}
