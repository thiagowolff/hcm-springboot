package br.com.litecode.web;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.time.*;
import java.time.format.DateTimeFormatter;

@FacesConverter("localDateTimeConverter")
public class LocalDateTimeConverter implements Converter {
	private static final String LOCAL_DATE_PATTERN = "dd/MM/yyyy";
	private static final String LOCAL_TIME_PATTERN = "HH:mm";

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}

		String pattern = getPattern(component);

		if (value instanceof LocalDate) {
			LocalDate dateValue = (LocalDate) value;
			return dateValue.format(DateTimeFormatter.ofPattern(pattern));
		}

		if (value instanceof LocalTime) {
			LocalTime dateValue = (LocalTime) value;
			return dateValue.format(DateTimeFormatter.ofPattern(pattern));
		}

		if (value instanceof Instant) {
			Instant dateValue = (Instant) value;
			return LocalDateTime.ofInstant(dateValue, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
		}

		LocalDateTime dateValue = (LocalDateTime) value;
		return dateValue.format(DateTimeFormatter.ofPattern(pattern));
	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
		String pattern = getPattern(component);

		if (pattern.equals(LOCAL_DATE_PATTERN)) {
			return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
		}

		if (pattern.equals(LOCAL_TIME_PATTERN)) {
			return LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern));
		}

		return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
	}

	private String getPattern(UIComponent component) {
		Object pattern = component.getAttributes().get("pattern");
		if (pattern == null) {
			pattern = LOCAL_DATE_PATTERN;
		}

		return (String) pattern;
	}
}
