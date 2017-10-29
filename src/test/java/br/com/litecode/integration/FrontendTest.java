package br.com.litecode.integration;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.integration.frontend.LoginPage;
import br.com.litecode.integration.frontend.MainPage;
import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.configuration.FluentConfiguration;
import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.seleniumhq.selenium.fluent.FluentWebDriver;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.*;

@Wait
@FluentConfiguration(webDriver = "chrome", baseUrl = "http://localhost:8888")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ComponentScan(basePackages = "br.com.litecode")
public class FrontendTest extends FluentTest {
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Page
	private LoginPage loginPage;

	@Page
	private MainPage mainPage;

	@Before
	public void setup() {
		window().setSize(new Dimension(1440,960));
	}

	@Test
	public void deleteSession() {
		loginPage.go();
		loginPage.fillAndSubmitForm("admin", "admin");
		assertThat(window().title()).isEqualTo("Serviço de Oxigenoterapia Hiperbárica");

		mainPage.selectDate(2017, 6, 2);
		mainPage.selectTime(8, 0);

		Patient patientX = patientRepository.findOne(1);
		mainPage.selectPatient(patientX.getPatientId());
		mainPage.getAddSessionButton().click();

		assertThat($("#form\\:chambersGrid tr.ui-widget-content[role='row']").size()).isEqualTo(1);

		$("button", withId().endsWith("deleteSessionButton")).click();
		$(".ui-confirm-dialog .btn-danger").click();
		await().atMost(10, TimeUnit.SECONDS).until($("#form\\:chambersGrid tr.ui-widget-content[role='row']")).not().present();

//		webDriver.navigate().to("http://localhost:8080");
//		assertThat(webDriver.getCurrentUrl()).isEqualTo("http://localhost:8080/login.xhtml");
//
//		webDriver.findElement(By.id("username")).sendKeys("thiago");
//		webDriver.findElement(By.id("password")).sendKeys("admin");
//		webDriver.findElement(By.id("loginButton")).click();
//
//		assertThat(webDriver.findElement(By.className("app-title")).getText()).isEqualTo("Serviço de Oxigenoterapia Hiperbárica");
//
//		javascriptExecutor.executeScript("PF('sessionDate').setDate(new Date(2017,0,1));");
//		javascriptExecutor.executeScript("PF('sessionDate').fireDateSelectEvent();");
//		//javascriptExecutor.executeScript("PF('sessionChamber').selectValue(0);");
//
//		javascriptExecutor.executeScript("PF('sessionTime').setTime('08:00');");
//
//		for (int i = 0; i < 5; i++) {
//			javascriptExecutor.executeScript("PF('sessionPatients').check($(PF('sessionPatients').checkboxes[" + i + "])); $(PF('sessionPatients').inputs[" + i + "]).prop('checked', true);");
//		}
//
//		webDriver.findElement(By.id("form:addButton")).click();
//		assertThat(fluentWebDriver.elements(By.cssSelector("#form\\:chambersGrid tr.ui-widget-content[role='row']")).size()).isEqualTo(1);
//
//		webDriver.findElement(By.id("form:chambersGrid:0:sessionsTable:0:deleteSessionButton")).click();
//		webDriver.findElement(By.cssSelector(".ui-confirm-dialog .btn-danger")).click();
//
//		assertThat(webDriver.findElements(By.cssSelector("#form\\:chambersGrid tr.ui-widget-content[role='row']")).size()).isEqualTo(0);
	}

	@After
	public void tearDown() {
		//webDriver.close();
	}
}