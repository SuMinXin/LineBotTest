package com.linebot.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableAspectJAutoProxy
public class RootConfig {
	
	@Bean
	public JedisPool jedisPool(RedisProperties redisProperties) {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxWaitMillis(redisProperties.getJedis().getPool().getMaxWait().toMillis());
		jedisPoolConfig.setMaxTotal(redisProperties.getJedis().getPool().getMaxActive());
		jedisPoolConfig.setMaxIdle(redisProperties.getJedis().getPool().getMaxIdle());
		jedisPoolConfig.setMinIdle(redisProperties.getJedis().getPool().getMinIdle());
		jedisPoolConfig.setJmxEnabled(true);
		return new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(),
				(int) redisProperties.getTimeout().toMillis(), redisProperties.getPassword(),
				redisProperties.getDatabase());
	}

}