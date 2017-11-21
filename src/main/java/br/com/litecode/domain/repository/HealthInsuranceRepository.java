package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.HealthInsurance;
import org.springframework.data.repository.CrudRepository;

public interface HealthInsuranceRepository extends CrudRepository<HealthInsurance, Integer> {
}