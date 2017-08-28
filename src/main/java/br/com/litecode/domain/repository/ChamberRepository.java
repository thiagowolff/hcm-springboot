package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Chamber;
import org.springframework.data.repository.CrudRepository;

public interface ChamberRepository extends CrudRepository<Chamber, Integer> {
}