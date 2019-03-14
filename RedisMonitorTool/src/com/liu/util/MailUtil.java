package com.liu.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.liu.config.LoadConfig;

public class MailUtil {
	public static final Logger logger = Logger.getLogger(MailUtil.class);
	
	public static String smtpServer = "";
    public static String smtpPort = "";
    public static String account = "";
    public static String password = "";
    
    private static Properties props = new Properties();
    static {
    	
    	 Properties properties = new Properties();
    	 // 使用InPutStream流读取properties文件
    	 BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader("conf/mail_server.properties"));
			properties.load(bufferedReader);
		} catch (Exception e) {
			logger.error("read mail_server.properties error: ", e);
		}
		smtpServer = properties.getProperty("smtp.server");
		smtpPort = properties.getProperty("smtp.port");
		account = properties.getProperty("account");  
		password = properties.getProperty("password");  
		    	
    	//创建邮件配置
        props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", smtpServer); // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.port", smtpPort); 
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.auth", "true"); // 需要请求认证
        props.setProperty("mail.smtp.ssl.enable", "true");// 开启ssl
    }
	
	public static void main(String[] args) throws Exception {
//		new MailUtil().sendEmail();
		System.out.println("sent ok");
	}

    public static void sendEmail(Map<String, String> messageInfoMap) {
        // 根据邮件配置创建会话，注意session别导错包
        Session session = Session.getDefaultInstance(props);
        // 开启debug模式，可以看到更多详细的输入日志
        session.setDebug(LoadConfig.getMailDebug());
        //创建邮件
        MimeMessage message = null;
		try {
			message = createEmail(session, messageInfoMap);
			 //获取传输通道
	        Transport transport = session.getTransport();
	        transport.connect(smtpServer, account, password);
	        //连接，并发送邮件
	        transport.sendMessage(message, message.getAllRecipients());
	        transport.close();
		} catch (Exception e) {
			logger.error("send mail error", e);
		}
    }


    private static  MimeMessage createEmail(Session session, Map<String, String> messageInfoMap) throws Exception {
        // 根据会话创建邮件
        MimeMessage msg = new MimeMessage(session);
        // 设置发送邮件方
        InternetAddress fromAddress = new InternetAddress(account, "Redis Reminder", "utf-8");
        msg.setFrom(fromAddress);
        
        // 设置邮件接收方
        InternetAddress receiveAddressQQ = new InternetAddress(messageInfoMap.get("mail"), "test", "utf-8");
        msg.setRecipient(RecipientType.TO, receiveAddressQQ);
        
        // 设置邮件标题
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        msg.setSubject("Redis告警-" + sdf.format(new Date()), "utf-8");
        msg.setText("Redis内存存在溢出风险，最大内存为" + messageInfoMap.get("maxmemoryHuman") 
        			+ ", 已使用内存为" + messageInfoMap.get("usedMemoryRssHuman") 
        			+ ", 使用率为" + messageInfoMap.get("rate")
        			+ ", 请及时处理！");
        // 设置显示的发件时间
        msg.setSentDate(new Date());
        // 保存设置
        msg.saveChanges();
        return msg;
    }
}
