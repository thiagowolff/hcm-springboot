package br.com.litecode.view;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
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
