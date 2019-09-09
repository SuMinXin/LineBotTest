package com.linebot.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class RedisClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

	private static final Integer EXPIRE_DAY = 15;

	@Autowired
	private JedisPool jedisPool;

	public void saveToken(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(key, value, "NX", "EX", (EXPIRE_DAY * 24 * 60 * 60));
		} catch (Exception e) {
			LOGGER.error("SaveToken Exception, msg={}", e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
	}

	public void saveHashToken(String hashKey, String key, String value) {
		Jedis jedis = null;
		try {
			Map<String, String> hash = new HashMap<>();
			hash.put(key, value);

			jedis = jedisPool.getResource();
			jedis.hmset(hashKey, hash);
			jedis.expire(hashKey, (EXPIRE_DAY * 24 * 60 * 60));
		} catch (Exception e) {
			LOGGER.error("SaveHashToken Exception, msg={}", e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
	}

	public void batchSaveToken(String key, Map<String, String> hash, int exipreSeconds) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.hmset(key, hash);
			jedis.expire(key, exipreSeconds);
		} catch (Exception e) {
			LOGGER.error("SaveHashToken Exception, msg={}", e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
	}

	public String retrieveToken(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.get(key);
		} catch (Exception e) {
			LOGGER.error("RetrieveToken Exception, msg={}", e.getMessage());
			return "";
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
	}

	public String retrieveHashToken(String hashKey, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.hmget(hashKey, key).get(0);
		} catch (Exception e) {
			LOGGER.error("RetrieveHashToken Exception, msg={}", e.getMessage());
			return "";
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
	}

	public List<String> retrieveHashToken(String hashKey) {
		Jedis jedis = null;
		List<String> result = new ArrayList<>();
		try {
			jedis = jedisPool.getResource();
			// ["Udbb24468127d89d1b43f54fb60517669:88515","Udbb24468127d89d1b43f54fb60517669:88511","Udbb24468127d89d1b43f54fb60517669:88522","Udbb24468127d89d1b43f54fb60517669:88510"]
			Set<String> keys = jedis.hkeys(hashKey);
			for (String s : keys) {
				result.add(retrieveHashToken(hashKey, s));
			}
		} catch (Exception e) {
			LOGGER.error("RetrieveHashToken By HashKey Exception, msg={}", e.getMessage());
		} finally {
			if (jedis != null)
				jedis.close(); // 歸還資源給JedisPool
		}
		return result;
	}
}