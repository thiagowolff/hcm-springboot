package br.com.litecode;

import br.com.litecode.config.ViewScope;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.faces.webapp.FacesServlet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@EnableAsync
@EnableCaching
@EntityScan(basePackageClasses = {Application.class, Jsr310JpaConverters.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean facesSservlet = new ServletRegistrationBean(new FacesServlet(), "*.xhtml", "/javax.faces.resource/*");
		facesSservlet.setAsyncSupported(true);
		return facesSservlet;
	}

	@Bean
	public FilterRegistrationBean characterEncodingFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.addInitParameter("encoding", "UTF-8");
		bean.addInitParameter("forceEncoding", "true");
		bean.setFilter(new CharacterEncodingFilter());
		bean.addUrlPatterns("/*");
		return bean;
	}

	@Bean
	public CustomScopeConfigurer viewScopeConfigurer() {
		CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
		Map<String, Object> scopes = new HashMap();
		scopes.put("view", new ViewScope());
		customScopeConfigurer.setScopes(scopes);
		return customScopeConfigurer;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	public CacheManager cacheManager(Ticker ticker) {
		CaffeineCache chartsCache = buildCache("charts", ticker, 5);
		CaffeineCache patientStatsCache = buildCache("patientStats", ticker, 15);

		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(chartsCache, patientStatsCache));
		return manager;
	}

	private CaffeineCache buildCache(String name, Ticker ticker, int minutesToExpire) {
		return new CaffeineCache(name, Caffeine.newBuilder()
				.expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
				.ticker(ticker)
				.build());
	}

	@Bean
	public Ticker ticker() {
		return Ticker.systemTicker();
	}
}
