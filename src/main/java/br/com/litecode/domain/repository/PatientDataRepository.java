package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.PatientData;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface PatientDataRepository<T extends PatientData> extends BaseCrudRepository<T, Integer> {
	List<T> findAllByOrderByNameAsc();
}