package br.com.litecode.controller;

import br.com.litecode.annotation.ScopeView;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.repository.ChamberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@ScopeView
@Component
public class ChamberController implements Serializable {
	@Autowired
	private ChamberRepository chamberRepository;

	private Iterable<Chamber> chambers;
	private Chamber chamber;

	public ChamberController() {
		chamber = new Chamber();
	}

	public Iterable<Chamber> getChambers() {
		if (chambers == null) {
			chambers = chamberRepository.findAll();
		}
		return chambers;
	}

	public Chamber getChamber() {
		return chamber;
	}

	public void setChamber(Chamber chamber) {
		this.chamber = chamber;
	}
}
