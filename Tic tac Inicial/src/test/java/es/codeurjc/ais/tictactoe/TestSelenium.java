package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;



public class TestSelenium {
    protected List<WebDriver> drivers = new ArrayList<WebDriver>();

    @BeforeAll
    public static void setupClass() { 
        WebDriverManager.chromedriver().setup();
        WebApp.start();
    }

    @AfterAll
    public static void teardownClass() {
            WebApp.stop();
    }

    @BeforeEach
    public void setupTest() {
    	//Creo 2 drivers porque cada jugador necesita 1 y los meto en la lista de drivers
        drivers.add(new ChromeDriver());
        drivers.add(new ChromeDriver());
    }

    @AfterEach
    public void teardown() {
    	for(WebDriver driver : drivers) {
            if (driver != null) {
                    driver.quit();
            }
    	}
    	drivers.clear();
    }
    
//Reutilizo los movimientos creados para el test de dobles
    @ParameterizedTest
	@MethodSource("values")
	public void TicTacSeleniumTest(int argument) throws InterruptedException {
    	ArrayList<Integer> mov1= new ArrayList<Integer>(argument);
		for(WebDriver driver : drivers) {
			driver.get("http://localhost:8080/");
		}
		//Inserto los nombres de los jugadores y doy click en el boton "play"
		drivers.get(0).findElement(By.id("nickname")).sendKeys("Pepe");
		drivers.get(0).findElement(By.id("startBtn")).click();

		drivers.get(1).findElement(By.id("nickname")).sendKeys("Juan");
		drivers.get(1).findElement(By.id("startBtn")).click();
		
		//Recorremos los movimientos que he prefijado
		for (int i=0;i<mov1.size();i++) {
			int turnoActual = i%2;
			drivers.get(turnoActual).findElement(By.id("cell-" + mov1.get(i))).click();
				//Esperar a que se abra la alerta
				Thread.sleep(500);
				if(mov1.get(0)==2 && mov1.get(1)==5) {
					assertThat(
						drivers.get(turnoActual).switchTo().alert().getText()
						).isEqualTo(
								"Pepe" + " wins! " + "Juan" + " looses.");
				}else if(mov1.get(0)==0 && mov1.get(1)==4 ) {
					assertThat(
							drivers.get(turnoActual).switchTo().alert().getText()
							).isEqualTo(
									"Juan" + " wins! " + "Pepe" + " looses.");
				}else if (mov1.get(0)==2 && mov1.get(1)==0) {
					assertThat(
						drivers.get(turnoActual).switchTo().alert().getText()
						).isEqualTo("Draw!");
			}
		}
	}
	public static Collection<Object[]> values() {
		 Object[][] values = {
		//Gana jugador 1
		 {2,5,4,1,6},
		 //Gana jugador 2
		 {0,4,3,6,1,2},
		 //Empate
		 {2,0,3,1,4,5,7,6,8}
		 };
		 return Arrays.asList(values);
		 } 
}