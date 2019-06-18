package br.com.litecode;

import br.com.litecode.domain.model.Session;
import br.com.litecode.service.timer.ControlledSessionTimeTicker;
import br.com.litecode.service.timer.TimeTicker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestConfig {
	@Bean
    @Primary
    public TimeTicker<Session> testTimeTicker() {
        return new ControlledSessionTimeTicker();
    }

    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    @Primary
    public JavaMailSender testMailService() {
        return new JavaMailSenderImpl();
    }
}