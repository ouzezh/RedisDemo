package com.ozz.redis;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
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
        StaticLog.info(StrUtil.toString(myRedisService.optimisticSet("myKey", "1", Duration.ofHours(1), "1")));
    }

}
