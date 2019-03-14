package com.liu.tool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.liu.config.LoadConfig;
import com.liu.util.InitJedisPool;
import com.liu.util.MailUtil;

public class MonitorTool {
	public static final Logger logger = Logger.getLogger(MonitorTool.class);
	
	public static void main(String[] args) {
		PropertyConfigurator.configure(System.getProperty("user.dir") + File.separator + "conf/log4j.properties");
		LoadConfig.loadMonitorNodes();
		InitJedisPool.init();
		
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				monitor();
			}
		};
		timer.schedule(timerTask, 0, 1000*LoadConfig.getInterval());
		
		logger.error("Start Success!");
	}
	
	public static void monitor() {
		for(Map.Entry<String, JedisPool> entry : InitJedisPool.getJedisPools().entrySet()) {
			Jedis jedis = null;
			try {
				jedis = entry.getValue().getResource();
			} catch (Exception e) {
				logger.error("[" + entry.getKey() + "] get jedis fail, please check the redis node is on", e);
				continue;
			}
			//获取jedis的memory相关信息
			String memoryStr = jedis.info("memory");
			String[] ss = memoryStr.split("\n");
			Map<String, String> infoMap = new HashMap<>();
			for (String str : ss) {
				if(str.startsWith("#")) {
					continue;
				}
				String[] strs = str.split(":");
				infoMap.put(strs[0], strs[1]);
			}
			
			//used_memory_rss_human
			//maxmemory_human
			//total_system_memory_human
			double usedMemoryRssHuman = Double.parseDouble(infoMap.get("used_memory_human").replace("G", ""));
			double maxmemoryHuman = Double.parseDouble(infoMap.get("maxmemory_human").replace("G", ""));
			if(usedMemoryRssHuman/maxmemoryHuman > LoadConfig.getThreshold()) {//当超过70%进行告警
				//TODO: 告警处理
				Map<String, String> messageInfoMap = new HashMap<>();
				messageInfoMap.put("mail", LoadConfig.getNodeToAdminMail().get(entry.getKey()));
				messageInfoMap.put("usedMemoryRssHuman", usedMemoryRssHuman+"G");
				messageInfoMap.put("maxmemoryHuman", maxmemoryHuman+"G");
				messageInfoMap.put("rate", String.format("%.2f", (usedMemoryRssHuman/maxmemoryHuman * 100)) + "%");
				MailUtil.sendEmail(messageInfoMap);
				
				logger.error("exceed threshold:" + messageInfoMap);
			}
		}
	}

}
