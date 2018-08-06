package com.idleroom.web.util;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Component
@Deprecated
public class EmailUtil {

    @Value("${spring.mail.username}")
    String from;
    @Value("${spring.mail.host}")
    String host;
    @Value("${spring.mail.password}")
    String password;
//    @Value("${spring.mail.port}")
//    int port;


    public void send(String title,String msg,String to) throws GeneralSecurityException, MessagingException {

        System.out.println(password);
        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = null;
            sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(from, password); //发件人邮件用户名、密码
            }
        });
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject(title);

            // 设置消息体
            message.setText(msg);

            // 发送消息
            Transport.send(message);
    }
}
