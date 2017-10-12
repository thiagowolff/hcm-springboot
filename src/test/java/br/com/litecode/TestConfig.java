package br.com.litecode;

import br.com.litecode.service.timer.Clock;
import br.com.litecode.service.timer.FakeSessionClock;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class TestConfig {
	@Bean
    public Clock sessionClock() {
        return new FakeSessionClock();
    }

    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    public JavaMailSender javaMailService() {
        return new JavaMailSenderImpl();
    }
}