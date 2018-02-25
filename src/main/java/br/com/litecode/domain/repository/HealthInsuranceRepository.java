package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.HealthInsurance;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HealthInsuranceRepository extends CrudRepository<HealthInsurance, Integer> {
	List<HealthInsurance> findAllByOrderByNameAsc();
}