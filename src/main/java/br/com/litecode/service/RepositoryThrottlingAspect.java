package br.com.litecode.service;

import br.com.litecode.service.timer.ChamberSessionTimer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

//@Aspect
//@Component
@Slf4j
public class RepositoryThrottlingAspect {
	@Value("${app.throttlingMaxDelay:2500}")
	private Long throttlingMaxDelay;

//    @After(value = "execution(* br.com.litecode.domain.repository..*(..))")
	public void throttle(JoinPoint joinPoint) {
		var stackTrace = StackWalker.getInstance().walk(sf -> sf.map(StackWalker.StackFrame::getClassName).collect(Collectors.toList()));
		if (stackTrace.contains(ChamberSessionTimer.class.getName())
				|| joinPoint.getSignature().getName().startsWith("convertTo")) {
			return;
		}
    	long sleepTime = ThreadLocalRandom.current().nextLong(0, throttlingMaxDelay);
		log.debug("Throttling call to {} for {}ms", joinPoint.getSignature().getName(), sleepTime);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}
}
