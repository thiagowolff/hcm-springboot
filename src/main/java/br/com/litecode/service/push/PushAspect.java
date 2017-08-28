package br.com.litecode.service.push;

import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PushAspect {
	@Autowired
	private PushService pushService;

	@After(value = "execution(* br.com.litecode..controller..*(..)) && @annotation(push))")
	public void pushRefresh(PushRefresh push) {
		String loggedUser = (String) SecurityUtils.getSubject().getPrincipal();
		pushService.publish(PushChannel.REFRESH, "", loggedUser);
	}
}
