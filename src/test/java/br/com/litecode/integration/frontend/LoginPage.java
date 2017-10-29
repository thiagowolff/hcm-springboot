package br.com.litecode.integration.frontend;

import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.annotation.PageUrl;
import org.fluentlenium.core.domain.FluentWebElement;
import org.openqa.selenium.support.FindBy;

import static org.assertj.core.api.Assertions.assertThat;

@PageUrl("login.xhtml")
public class LoginPage extends FluentPage {
	@FindBy(css = "#loginButton")
	private FluentWebElement loginButton;

	public void fillAndSubmitForm(String... paramsOrdered) {
		$("input").fill().with(paramsOrdered);
		loginButton.click();
	}
}
