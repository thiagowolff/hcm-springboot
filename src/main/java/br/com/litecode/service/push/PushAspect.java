package br.com.litecode.service.push;

import br.com.litecode.domain.model.User;
import br.com.litecode.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PushAspect {
	private PushService pushService;

	@Autowired
    public PushAspect(PushService pushService) {
        this.pushService = pushService;
    }

    @After(value = "execution(* br.com.litecode..controller..*(..)) && @annotation(push))")
	public void pushRefresh(JoinPoint joinPoint, PushRefresh push) {
		User loggedUser = UserPrincipal.getLoggedUser();
		pushService.publish(PushChannel.REFRESH, "", loggedUser);

		if (loggedUser != null) {
            log.info("[{}] {}", loggedUser.getUsername(), joinPoint.getSignature().getName());
        }
	}
}
