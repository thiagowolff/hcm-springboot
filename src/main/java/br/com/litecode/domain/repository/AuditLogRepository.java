package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.AuditLog;
import org.springframework.data.repository.CrudRepository;

public interface AuditLogRepository extends CrudRepository<AuditLog, Integer> {
}