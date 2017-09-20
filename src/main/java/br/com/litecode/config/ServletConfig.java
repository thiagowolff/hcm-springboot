package br.com.litecode.config;

import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Messages;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Configuration
@Slf4j
public class ServletConfig extends WebMvcConfigurerAdapter implements ServletContextInitializer, EmbeddedServletContainerCustomizer {
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean facesServlet = new ServletRegistrationBean(new FacesServlet(), "*.xhtml", "/javax.faces.resource/*");
		facesServlet.setAsyncSupported(true);
		return facesServlet;
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		initMessageResolver();

		servletContext.setInitParameter("litefaces.ENUM_MESSAGE_BUNDLE", "br.com.litecode.enums");
		servletContext.setInitParameter("litefaces.ENUM_KEY_PREFIX", "enum");

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

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.xhtml");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		super.addViewControllers(registry);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
	}
}