package br.com.litecode.config;

import br.com.litecode.controller.NavigationController;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.filter.CacheControlFilter;
import org.omnifaces.util.Messages;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.el.ELException;
import javax.faces.application.ViewExpiredException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Configuration
@Slf4j
public class ServletConfig implements WebMvcConfigurer, ServletContextInitializer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	@Bean
	public FilterRegistrationBean cacheControlRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CacheControlFilter());
		registration.addUrlPatterns("*.png");
		registration.addInitParameter("expires", "30d");
		registration.setName("cache30days");
		return registration;
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		initMessageResolver();

//		servletContext.setInitParameter("litefaces.ENUM_MESSAGE_BUNDLE", "enums");
//		servletContext.setInitParameter("litefaces.ENUM_KEY_PREFIX", "enum");
//		servletContext.setInitParameter("primefaces.MOVE_SCRIPTS_TO_BOTTOM", "false");
//		servletContext.setInitParameter("org.apache.myfaces.SUPPORT_MANAGED_BEANS", "false");
//		servletContext.setInitParameter("org.apache.myfaces.INIT_PARAM_CACHE_EL_EXPRESSIONS", "alwaysRecompile");

		try {
			ResourceBundle versionBundle = ResourceBundle.getBundle("version");
			LocalDate versionDate = LocalDate.parse(versionBundle.getString("versionDate"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			String[] version = versionBundle.getString("version").split("\\.");
			servletContext.setAttribute("versionMajor", version[0]);
			servletContext.setAttribute("versionMinor", version[1]);
			servletContext.setAttribute("versionPatch", version[2]);
			servletContext.setAttribute("versionDate", versionDate);
		} catch (Exception e) {
			log.warn("Unable to load version date", e);
		}
	}

	private void initMessageResolver() {
		final ResourceBundle bundle = ResourceBundle.getBundle("messages");

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

		for (String page : NavigationController.pageLinkMapping.keySet()) {
			registry.addViewController(page).setViewName("forward:/index.xhtml");
		}
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		factory.addErrorPages(new ErrorPage(ViewExpiredException.class, "/login.xhtml"));
		factory.addErrorPages(new ErrorPage(ELException.class, "/errorPage.xhtml"));
		factory.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errorPage.xhtml"));
		factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/pageNotFound.xhtml"));
		factory.getMimeMappings().add("less", "plain/text");
	}
}