package br.com.litecode.view;

import br.com.caelum.stella.validation.CPFValidator;
import com.google.common.base.Strings;
import org.omnifaces.util.Messages;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("cpfValidator")
public class CpfValidator implements Validator {
	@Override
	public void validate(FacesContext context, UIComponent arg1, Object object) throws ValidatorException {
		String cpf = (String) object;
		
		if (Strings.isNullOrEmpty(cpf)) {
			return;
		}
		
		CPFValidator cpfValidator = new CPFValidator(false);
		try {
			cpfValidator.assertValid(cpf);
		} catch(Exception e) {
			throw new ValidatorException(Messages.createError("error.invalidCPF"));
		}
	}
}
