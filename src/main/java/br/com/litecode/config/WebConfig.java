package br.com.litecode.config;

import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CharacterEncodingFilter;
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
public class WebConfig {
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean facesSservlet = new ServletRegistrationBean(new FacesServlet(), "*.xhtml", "/javax.faces.resource/*");
		facesSservlet.setAsyncSupported(true);
		return facesSservlet;
	}

//	@Bean
	public FilterRegistrationBean characterEncodingFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.addInitParameter("encoding", "UTF-8");
		bean.addInitParameter("forceEncoding", "true");
		bean.setFilter(new CharacterEncodingFilter());
		bean.addUrlPatterns("/*");
		return bean;
	}

//	@Bean
//	public CustomScopeConfigurer viewScopeConfigurer() {
//		CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
//		Map<String, Object> scopes = new HashMap<>();
//		scopes.put("view", new ViewScope());
//		customScopeConfigurer.setScopes(scopes);
//		return customScopeConfigurer;
//	}
}

@Configuration
@Slf4j
class WebContextInitializer implements ServletContextInitializer {
	@Autowired
	Environment environment;

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		initMessageResolver();

//		servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
//		servletContext.setInitParameter("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
//		servletContext.setInitParameter("primefaces.THEME", "admin");
//		servletContext.setInitParameter("primefaces.SUBMIT", "partial");
//		servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
//		servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", "true");
		servletContext.setInitParameter("litefaces.ENUM_MESSAGE_BUNDLE", "br.com.litecode.enums");
		servletContext.setInitParameter("litefaces.ENUM_KEY_PREFIX", "enum");
//		servletContext.setInitParameter("javax.servlet.jsp.jstl.fmt.localizationContext", "resources.application");
//		servletContext.setInitParameter("org.apache.myfaces.SERIALIZE_STATE_IN_SESSION", "false");
//		servletContext.setInitParameter("javax.faces.FACELETS_BUFFER_SIZE", "65535");

//		if (Arrays.asList(environment.getActiveProfiles()).contains("development")) {
//			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
//			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");
//		} else {
//			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
//			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");
//		}

		log.info("Project stage set to {}", servletContext.getInitParameter("javax.faces.PROJECT_STAGE"));

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

@Configuration
class ServletContainerCustomizer extends WebMvcConfigurerAdapter implements EmbeddedServletContainerCustomizer {
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.xhtml");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		super.addViewControllers(registry);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
//		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
//		mappings.add("eot", "application/vnd.ms-fontobject");
//		mappings.add("otf", "font/opentype");
//		mappings.add("ttf", "application/x-font-ttf");
//		mappings.add("woff", "application/x-font-woff");
//		mappings.add("svg", "image/svg+xml");
//		mappings.add("woff2", "application/x-font-woff2");
//		container.setMimeMappings(mappings);
	}
}