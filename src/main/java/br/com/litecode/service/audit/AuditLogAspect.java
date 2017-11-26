package br.com.litecode.service.audit;

import br.com.litecode.domain.model.AuditLog;
import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.AuditLogRepository;
import br.com.litecode.service.audit.Audit.OperationType;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {
	@Autowired
	private AuditLogRepository auditLogRepository;

	@Around(value = "execution(* br.com.litecode..*(..)) && @annotation(audit))")
	public Object audit(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
		User loggedUser = Faces.getSessionAttribute("loggedUser");

		Object entity = joinPoint.proceed();
		if (audit.value() == OperationType.CREATE) {

		}

		AuditLog auditLog = new AuditLog(joinPoint.getSignature().getName(), loggedUser);
		auditLogRepository.save(auditLog);

		return entity;
	}
}
