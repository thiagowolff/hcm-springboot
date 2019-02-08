package br.com.litecode.service.cache;

import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.User;
import br.com.litecode.security.UserPrincipal;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
		sessionCache.evict(Arrays.asList(session.getChamber().getChamberId(), session.getSessionDate()));
		sessionCache.evict(Arrays.asList(session.getSessionDate().getYear(), session.getSessionDate().getMonthValue()));

		Cache patientCache = cacheManager.getCache("patient");
		patientCache.evict(Arrays.asList(session.getSessionId(), session.getSessionDate()));
		session.getPatientSessions().stream()
				.map(PatientSession::getPatientId)
				.collect(Collectors.toList())
				.forEach(patientCache::evict);

		Cache chartCache = cacheManager.getCache("chart");
		chartCache.clear();
	}
}