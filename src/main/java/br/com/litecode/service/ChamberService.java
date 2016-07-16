package br.com.litecode.service;

import br.com.litecode.domain.Chamber;
import br.com.litecode.persistence.impl.ChamberDao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class ChamberService {
	@Inject	private ChamberDao chamberDao;

	public List<Chamber> getChambers() {
		return chamberDao.findAll();
	}
}
