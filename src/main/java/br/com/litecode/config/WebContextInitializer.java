package br.com.litecode.config;

import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;

@Configuration
@Slf4j
public class WebContextInitializer implements ServletContextInitializer {
	@Autowired
	Environment environment;

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		initMessageResolver();

		servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
		servletContext.setInitParameter("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
		servletContext.setInitParameter("primefaces.THEME", "hcm");
		servletContext.setInitParameter("primefaces.SUBMIT", "partial");
		servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
		servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", "true");
		servletContext.setInitParameter("litefaces.ENUM_MESSAGE_BUNDLE", "br.com.litecode.enums");
		servletContext.setInitParameter("litefaces.ENUM_KEY_PREFIX", "enum");
		servletContext.setInitParameter("javax.servlet.jsp.jstl.fmt.localizationContext", "resources.application");
		servletContext.setInitParameter("org.apache.myfaces.SERIALIZE_STATE_IN_SESSION", "false");
		servletContext.setInitParameter("javax.faces.FACELETS_BUFFER_SIZE", "65535");

		if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");
		} else {
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");
		}

		try {
			ResourceBundle versionBundle = ResourceBundle.getBundle("version");
			LocalDate versionDate = LocalDate.parse(versionBundle.getString("versionDate"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			servletContext.setAttribute("versionDate", versionDate);
		} catch (Exception e) {
			log.warn("Unable to load version date", e);
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
}