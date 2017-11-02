package br.com.litecode;

import br.com.litecode.service.timer.TimeTicker;
import br.com.litecode.service.timer.ControlledSessionTimeTicker;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class TestConfig {
	@Bean
    public TimeTicker sessionClock() {
        return new ControlledSessionTimeTicker();
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