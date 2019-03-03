package br.com.litecode.controller;

import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SessionScoped
@Component
public class NavigationController implements Serializable {
	public static final Map<String, String> pageLinkMapping = new HashMap<>();

	static {
		pageLinkMapping.put("/", "/sessions.xhtml");
		pageLinkMapping.put("/patients", "/patients.xhtml");
		pageLinkMapping.put("/chambers", "/chambers.xhtml");
		pageLinkMapping.put("/charts", "/charts.xhtml");
		pageLinkMapping.put("/users", "/users.xhtml");
		pageLinkMapping.put("/alarms", "/alarms.xhtml");
		pageLinkMapping.put("/system", "/system.xhtml");
	}

	private String activePage = "/sessions.xhtml";

	public void updateNavigation() {
		String currentPage = Faces.getRequestParameter("activePage");
		String update = Faces.getRequestParameter("update");

		String pageLink = pageLinkMapping.get(currentPage);

		if (pageLink == null) {
			log.warn("Page {} not mapped for ajax navigation.", currentPage);
			activePage = "/sessions.xhtml";
			return;
		}

		if (pageLink.equals(activePage)) {
			return;
		}
		activePage = pageLink;

		if (update != null) {
			PrimeFaces.current().ajax().update(update);
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
