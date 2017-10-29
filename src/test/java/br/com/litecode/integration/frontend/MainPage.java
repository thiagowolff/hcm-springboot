package br.com.litecode.integration.frontend;

import lombok.Getter;
import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.annotation.PageUrl;
import org.fluentlenium.core.domain.FluentWebElement;
import org.openqa.selenium.support.FindBy;

import static com.google.common.base.Strings.padStart;
import static java.lang.String.valueOf;

@PageUrl("/")
@Getter
public class MainPage extends FluentPage {
	@FindBy(css = "#form\\:addButton")
	private FluentWebElement addSessionButton;

	public void selectDate(int year, int month, int day) {
		executeScript("PF('sessionDate').setDate(new Date(" + year + "," + (month - 1) + "," + day + "));");
		executeScript("PF('sessionDate').fireDateSelectEvent();");
	}

	public void selectTime(int hour, int minute) {
		String time = padStart(valueOf(hour), 2, '0') + ":" + padStart(valueOf(minute), 2, '0');
		executeScript("PF('sessionTime').setTime('" + time + "');");
	}

	public void selectPatient(int patientId) {
		executeScript("$($.grep(PF('sessionPatients').inputs, function(item) { return item.value.indexOf('[" + patientId + "]') != -1; })[0]).prop('checked', true);");
	}
}
