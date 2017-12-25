package br.com.litecode.controller;

import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ViewScoped
@Component
public class NavigationController implements Serializable {
	public static final Map<String, String> pageLinkMapping = new HashMap<>();

	static {
		pageLinkMapping.put("/", "/sessions.xhtml");
		pageLinkMapping.put("/patients", "/patients.xhtml");
		pageLinkMapping.put("/chambers", "/chambers.xhtml");
		pageLinkMapping.put("/charts", "/charts.xhtml");
		pageLinkMapping.put("/users", "/users.xhtml");
	}

	private String activePage = "/sessions.xhtml";

	public void updateNavigation() {
		String currentPage = Faces.getRequestParameter("activePage");
		String update = Faces.getRequestParameter("update");

		String pageLink = pageLinkMapping.get(currentPage);
		if (pageLink.equals(activePage)) {
			return;
		}
		activePage = pageLink;

		if (update != null) {
			RequestContext.getCurrentInstance().update(update);
		}
	}

	public String getActivePage() {
		return activePage;
	}
	
	public void setActivePage(String activePage) {
		if (activePage != null) {
			this.activePage = activePage;
		}
	}
}
