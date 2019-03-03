package br.com.litecode;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
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

import java.time.Clock;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@EnableAsync
@EnableCaching
@EntityScan(basePackageClasses = { Application.class, Jsr310JpaConverters.class })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	public CacheManager cacheManager(Ticker ticker) {
		CaffeineCache chamberCache = buildCache("chamber", ticker, 60 * 24 * 7);
		CaffeineCache sessionCache = buildCache("session", ticker, 60);
		CaffeineCache patientCache = buildCache("patient", ticker, 60);
		CaffeineCache patientDataCache = buildCache("patientData", ticker, 60 * 24);
		CaffeineCache chartCache = buildCache("chart", ticker, 60 * 4);
		CaffeineCache alarmCache = buildCache("alarm", ticker, 60 * 24);

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(chamberCache, sessionCache, patientCache, patientDataCache, chartCache, alarmCache));
		return cacheManager;
	}

	private CaffeineCache buildCache(String name, Ticker ticker, int minutesToExpire) {
		return new CaffeineCache(name, Caffeine.newBuilder()
				.expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
				.ticker(ticker)
				.build());
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	public Ticker ticker() {
		return Ticker.systemTicker();
	}
}
