package com.liu.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class LoadConfig {
	public static final Logger logger = Logger.getLogger(LoadConfig.class);
	private static List<String> monitorNodesList = new ArrayList<String>();
	private static Map<String, String> nodeToAdminMailMap = new HashMap<>();
	private static double threshold = 0;
	private static int interval = 0;
	private static boolean mailDebug = false;
	
	public static void main(String[] args) {
		loadMonitorNodes();
	}
	
	/**
	 * 从配置文件加载待监控节点
	 */
	public static void loadMonitorNodes() {
		 Properties properties = new Properties();
    	 // 使用InPutStream流读取properties文件
    	 BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader("conf/monitor.properties"));
			properties.load(bufferedReader);
		} catch (Exception e) {
			logger.error("read monitor.properties error: ", e);
		}
		
		String line = properties.getProperty("monitor.nodes").replaceAll(" ", "");
		if(line.length() == 0) {
			return;
		}
		String[] ss = line.split(";");
		for (String str : ss) {
			if(str.length() == 0) {
				continue;
			}
			String[] strs = str.split("-");
			if(strs.length < 2) {
				continue;
			}
			monitorNodesList.add(strs[0]);
			nodeToAdminMailMap.put(strs[0], strs[1]);
		}
		
		//获取阈值
		threshold = Double.parseDouble(String.valueOf(properties.get("threshold")));
		interval = Integer.parseInt(String.valueOf(properties.get("monitor.interval")));
		mailDebug = Boolean.parseBoolean(String.valueOf(properties.get("mail.debug")));
	}
	
	
	public static List<String> getMonitorNodes() {
		return monitorNodesList;
	}
	
	public static Map<String, String> getNodeToAdminMail() {
		return nodeToAdminMailMap;
	}

	public static double getThreshold() {
		return threshold;
	}
	
	public static int getInterval() {
		return interval;
	}
	
	public static boolean getMailDebug() {
		return mailDebug;
	}
	
}
