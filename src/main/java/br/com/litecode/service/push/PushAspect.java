package br.com.litecode.service.push;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PushAspect {
	@Autowired
	private PushService pushService;

	@After(value = "execution(* br.com.litecode..controller..*(..)) && @annotation(push))")
	public void pushRefresh(JoinPoint joinPoint, PushRefresh push) {
		String loggedUser = (String) SecurityUtils.getSubject().getPrincipal();
		pushService.publish(PushChannel.REFRESH, "", loggedUser);

		log.info("[{}] {}", loggedUser, joinPoint.getSignature().getName());
	}
}
