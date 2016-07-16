package br.com.litecode.view;

import br.com.caelum.stella.format.CPFFormatter;
import com.google.common.base.Strings;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("cpfConverter")
public class CpfConverter implements Converter {
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		return value.replaceAll("[^0-9]", "");
	}
	
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		String cpf = (String) value;
		if (Strings.isNullOrEmpty(cpf)) {
			return null;
		}
		return new CPFFormatter().format(cpf);
	}
}