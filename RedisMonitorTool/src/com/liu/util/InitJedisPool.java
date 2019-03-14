package com.liu.util;

import java.util.HashMap;
import java.util.Map;

import com.liu.config.LoadConfig;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class InitJedisPool {
	private static Map<String, JedisPool> jedisPoolMap = new HashMap<>();
	
	/**
	 * 初始化每个监控节点的jedispool
	 */
	public static void init() {
		JedisPoolConfig config = new JedisPoolConfig();
		// 设置最大连接数
		config.setMaxTotal(500);
		// 设置最大空闲数
		config.setMaxIdle(8);
		// 设置最大等待时间
		config.setMaxWaitMillis(1000 * 600);
		// 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
		config.setTestOnBorrow(true);
		
		for (String nodeStr : LoadConfig.getMonitorNodes()) {
			String[] ss = nodeStr.split(":");
			if(ss.length < 2) {
				continue;
			}
			JedisPool jedisPool = new JedisPool(config, ss[0], Integer.parseInt(ss[1]), 3000);
			jedisPoolMap.put(nodeStr, jedisPool);
		}
	}
	
	public static Map<String, JedisPool> getJedisPools() {
		return jedisPoolMap;
	}

}
