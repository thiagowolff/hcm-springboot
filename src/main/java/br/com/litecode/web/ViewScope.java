package br.com.litecode.web;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;

public class ViewScope implements Scope {

	public Object get(String name, ObjectFactory objectFactory) {
		Map<String, Object> viewMap = getViewMap();
		Object bean = viewMap.computeIfAbsent(name, k -> objectFactory.getObject());
		return bean;
	}

	public Object remove(String name) {
		Map<String, Object> viewMap = getViewMap();
		Object bean = viewMap.get(name);

		if (bean != null) {
			viewMap.remove(name);
		}

		return bean;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
	}

	public String getConversationId() {
		return null;
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	private Map<String, Object> getViewMap() {
		if (FacesContext.getCurrentInstance() == null) {
			return new HashMap<>();
		}
		return FacesContext.getCurrentInstance().getViewRoot().getViewMap();
	}
}
