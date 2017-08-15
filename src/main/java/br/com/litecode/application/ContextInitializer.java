package br.com.litecode.application;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.omnifaces.util.Messages;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

@WebListener
public class ContextInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.setProperty("org.apache.el.parser.COERCE_TO_ZERO", "false");

		initMessageResolver();

		try {
			ResourceBundle versionBundle = ResourceBundle.getBundle("version");
			LocalDate versionDate = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(versionBundle.getString("versionDate"));
			event.getServletContext().setAttribute("newVersion", LocalDate.now().isBefore(versionDate.plusDays(3)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initMessageResolver() {
		final ResourceBundle bundle = ResourceBundle.getBundle("br.com.litecode.messages");
		
		Messages.setResolver((message, params) -> {
			if (bundle.containsKey(message)) {
				message = bundle.getString(message);
			}
			return MessageFormat.format(message, params);
		});
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
}
