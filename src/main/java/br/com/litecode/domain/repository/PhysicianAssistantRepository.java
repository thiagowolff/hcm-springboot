package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.PhysicianAssistant;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhysicianAssistantRepository extends CrudRepository<PhysicianAssistant, Integer> {
	List<PhysicianAssistant> findAllByOrderByNameAsc();
}