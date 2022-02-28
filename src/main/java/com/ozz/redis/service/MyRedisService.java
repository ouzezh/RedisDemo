package com.ozz.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class MyRedisService {
    @Autowired
    private StringRedisTemplate rt;

    public String get(String key) {
        return rt.opsForValue().get(key);
    }

    public void set(String key, String value, Duration duration) {
        rt.opsForValue().set(key, value, duration);
    }

    public void delete(String key) {
        rt.delete(key);
    }

    public boolean optimisticSet(String key, String version, Duration duration, String beforeVersion) {
        SessionCallback<List<Object>> callback = new SessionCallback<>() {
            @Override
            public <K, V> List<Object> execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                rt.watch(key);
                String ver = rt.opsForValue().get(key);
                rt.multi();// 开启事务
                try {
                    if (!MyRedisService.this.equals(beforeVersion, ver)) {
                        return Collections.emptyList();
                    }
                    rt.opsForValue().set(key, version, duration);
                    return rt.exec();// 执行任务队列里所有命令，并结束事务
                } catch (Exception e) {
                    rt.discard();// 取消事务并执行unwatch
                    throw e;
                }
            }
        };
        List<Object> list = rt.execute(callback);
        return Objects.requireNonNull(list).size() == 1 && Boolean.TRUE.equals(list.get(0));
    }

    private boolean equals(String beforeVersion, String ver) {
        if (beforeVersion == null) {
            return ver == null;
        }
        return beforeVersion.equals(ver);
    }
}
