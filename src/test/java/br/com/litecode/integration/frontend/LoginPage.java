package br.com.litecode.integration.frontend;

import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.annotation.PageUrl;

import static org.assertj.core.api.Assertions.assertThat;

@PageUrl("login.xhtml")
public class LoginPage extends FluentPage {
	@Override
	public void isAt() {
		assertThat(window().title()).isEqualTo("Serviço de Oxigenoterapia Hiperbárica");
	}


	public void fillAndSubmitForm(String... paramsOrdered) {
		$("input").fill().with(paramsOrdered);
		$("#loginButton").click();
	}
}
