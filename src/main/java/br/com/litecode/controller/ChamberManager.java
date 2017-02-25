package br.com.litecode.controller;

import br.com.litecode.domain.Chamber;
import br.com.litecode.service.ChamberService;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ChamberManager implements Serializable {
	private ChamberService chamberService;

	private List<Chamber> chambers;
	private Chamber chamber;

	public ChamberManager() {
		chamber = new Chamber();
	}

	public List<Chamber> getChambers() {
		if (chambers == null) {
			chambers = chamberService.getChambers();
		}
		return chambers;
	}

	@Inject
	public void setChamberService(ChamberService chamberService) {
		this.chamberService = chamberService;
	}

	public Chamber getChamber() {
		return chamber;
	}

	public void setChamber(Chamber chamber) {
		this.chamber = chamber;
	}
}
