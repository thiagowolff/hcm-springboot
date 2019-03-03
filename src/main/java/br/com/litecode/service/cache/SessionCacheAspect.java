package br.com.litecode.service.cache;

import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class SessionCacheAspect {

	@Autowired
	private CacheManager cacheManager;

    @After(value = "execution(* br.com.litecode..*(..)) && @annotation(sessionCacheEvict))")
	public void evictSessionCache(JoinPoint joinPoint, SessionCacheEvict sessionCacheEvict) {
		Object argument = Arrays.stream(joinPoint.getArgs()).filter(arg -> arg instanceof Session || arg instanceof PatientSession).findFirst().orElse(null);

		Session session;
		if (argument instanceof Session) {
			session = (Session) argument;
		} else if (argument instanceof PatientSession) {
			session = ((PatientSession) argument).getSession();
		} else {
			return;
		}

		Cache sessionCache = cacheManager.getCache("session");
		sessionCache.evict(new SimpleKey(session.getChamber().getChamberId(), session.getSessionDate()));
		sessionCache.evict(new SimpleKey(session.getSessionDate().getYear(), session.getSessionDate().getMonthValue()));

		Cache patientCache = cacheManager.getCache("patient");
		patientCache.clear();

		Cache chartCache = cacheManager.getCache("chart");
		chartCache.clear();
	}
}