package com.ozz.redis.direct.sentinel;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class JedisSentinelTemplate implements InitializingBean, DisposableBean {

  private JedisSentinelPool pool;
  @Value("${spring.redis.sentinel.nodes}")
  private String nodes;
  @Value("${spring.redis.sentinel.master}")
  private String master;
  @Value("${spring.redis.password}")
  private String passWord;
  @Value("${spring.redis.timeout}")
  private Duration timeOut;

  public static void main(String[] args) throws Exception {
    JedisSentinelTemplate jst = new JedisSentinelTemplate();
    jst.setNodes("localhost:26379,localhost2:26379");
    jst.setMaster("masterName");
    jst.setPassWord("");
    jst.setTimeOut(Duration.ofMinutes(30));
    jst.afterPropertiesSet();

    StaticLog.info("-start-");

    StaticLog.info(jst.get("x"));

    // print info
    String[] node = jst.nodes.replaceAll("\\s", "").split(",")[0].split(":");
    try (Jedis jedis = new Jedis(node[0], Integer.valueOf(node[1]))) {
      List<Map<String, String>> list = jedis.sentinelSlaves(jst.master);
      StaticLog.info(String.format("master info: %s, %s", jst.master, jst.pool.getCurrentHostMaster()));
      for (int i = 0; i < list.size(); i++) {
        StaticLog.info(String.format("\tslave %s: %s", i + 1, list.get(i).get("name")));
      }
    }

    StaticLog.info("-end-");

    jst.destroy();
  }

  @Override
  public void destroy() throws Exception {
    if (pool != null && !pool.isClosed()) {
      pool.close();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    poolConfig.setMaxWaitMillis(this.timeOut.toMillis());

    Set<String> nodeSet = Arrays
        .stream(this.nodes.replaceAll("\\s", "").split(","))
        .collect(Collectors.toSet());

    pool = new JedisSentinelPool(this.master, nodeSet, poolConfig, StrUtil.emptyToNull(this.passWord));
  }

  public String get(String key) {
    try (Jedis jedis = pool.getResource()) {
      return jedis.get(key);
    }
  }
}
