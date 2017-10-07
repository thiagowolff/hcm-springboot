package br.com.litecode.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class MailService {
	@Autowired
	private JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String text) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		try {
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(text);
			mailSender.send(message);
		} catch (MessagingException e) {
			log.error("Unable to send email: {}", e);
		}
	}
}
