package edu.atilim.acma.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class ACMAUtil {
	public static final Random RANDOM = new Random();
	public static final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	public static String splitCamelCase(String in) {
		StringBuilder sb = new StringBuilder();
		
		for (char c : in.toCharArray()) {
			if (c >= 'A' && c <= 'Z' && sb.length() > 0)
				sb.append(' ');
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deepCopy(T item) {
		if (item == null) return null;
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(item);
			oos.flush();
			
			byte[] rawData = baos.toByteArray();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(rawData);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (T)ois.readObject();
		} catch(Exception e) {
		}
		
		return null;
	}
	
	public static void sendMail(final String recipient, final String subject, final String message) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Properties props = new Properties();
					props.put("mail.smtp.host", "mail.a-cma.com");
					props.put("mail.smtp.user", "web+a-cma.com");
					props.put("mail.smtp.password", "web253263");
					props.put("mail.smtp.auth", "true");
					props.put("mail.smtp.port", "26");
					
					Session session = Session.getDefaultInstance(props, null);
					
					MimeMessage mm = new MimeMessage(session);
					mm.setFrom(new InternetAddress("web@a-cma.com"));
					mm.addRecipient(RecipientType.TO, new InternetAddress(recipient));
					mm.setSubject(subject);
					mm.setText(message);
					
					Transport transport = session.getTransport("smtp");
					transport.connect("mail.a-cma.com", "web+a-cma.com", "web253263");
					transport.sendMessage(mm, mm.getAllRecipients());
					transport.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
