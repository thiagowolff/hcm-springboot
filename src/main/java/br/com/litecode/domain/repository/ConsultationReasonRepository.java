package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.ConsultationReason;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConsultationReasonRepository extends CrudRepository<ConsultationReason, Integer> {
	List<ConsultationReason> findAllByOrderByDescriptionAsc();
}