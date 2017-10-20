package br.com.litecode.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class FrontendTest {

	private WebDriver webDriver;
	private JavascriptExecutor javascriptExecutor;

	@Before
	public void setup() {
		webDriver = new ChromeDriver();
		javascriptExecutor = (JavascriptExecutor) webDriver;

		webDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		webDriver.manage().window().setSize(new Dimension(1440,960));
	}

	@Test
	public void sessionTest() {
		webDriver.navigate().to("http://localhost:8080");
		assertThat(webDriver.getCurrentUrl()).isEqualTo("http://localhost:8080/login.xhtml");

		webDriver.findElement(By.id("username")).sendKeys("thiago");
		webDriver.findElement(By.id("password")).sendKeys("admin");
		webDriver.findElement(By.id("loginButton")).click();

		assertThat(webDriver.findElement(By.className("app-title")).getText()).isEqualTo("Serviço de Oxigenoterapia Hiperbárica");

		javascriptExecutor.executeScript("PF('sessionDate').setDate(new Date(2017,0,1));");
		javascriptExecutor.executeScript("PF('sessionDate').fireDateSelectEvent();");
		//javascriptExecutor.executeScript("PF('sessionChamber').selectValue(0);");

		javascriptExecutor.executeScript("PF('sessionTime').setTime('08:00');");

		for (int i = 0; i < 5; i++) {
			javascriptExecutor.executeScript("PF('sessionPatients').check($(PF('sessionPatients').checkboxes[" + i + "])); $(PF('sessionPatients').inputs[" + i + "]).prop('checked', true);");
		}

		webDriver.findElement(By.id("form:addButton")).click();
		assertThat(webDriver.findElements(By.cssSelector("#form\\:chambersGrid tr.ui-widget-content[role='row']")).size()).isEqualTo(1);

		webDriver.findElement(By.id("form:chambersGrid:0:sessionsTable:0:deleteSessionButton")).click();
		webDriver.findElement(By.cssSelector(".ui-confirm-dialog .btn-danger")).click();

		assertThat(webDriver.findElements(By.cssSelector("#form\\:chambersGrid tr.ui-widget-content[role='row']")).size()).isEqualTo(0);
	}

	@After
	public void tearDown() {
		webDriver.close();
	}
}