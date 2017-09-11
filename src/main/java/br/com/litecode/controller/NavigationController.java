package br.com.litecode.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Scope("view")
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
