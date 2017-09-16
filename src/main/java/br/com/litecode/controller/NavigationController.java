package br.com.litecode.controller;

import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class NavigationController implements Serializable {
	private String activePage = "/session.xhtml";

	public String getActivePage() {
		return activePage;
	}
	
	public void setActivePage(String activePage) {
		if (activePage != null) {
			this.activePage = activePage;
		}
	}
}
