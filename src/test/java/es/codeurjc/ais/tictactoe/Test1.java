package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import es.codeurjc.ais.tictactoe.TicTacToeGame.CellMarkedValue;
import es.codeurjc.ais.tictactoe.TicTacToeGame.EventType;
import es.codeurjc.ais.tictactoe.TicTacToeGame.WinnerValue;
import io.github.bonigarcia.wdm.WebDriverManager;




public class Test1 {
	private Board b;
	
	@BeforeEach
	public void init() {
		b=new Board(); 
	}
	
	@Test
	public void testchekifcellswinner_Player1() {
				int[] posPlayer1 = { 0, 3, 6 };
				int[] posPlayer2 = { 2, 5, 7 };
				
				for(int i=0;i<posPlayer1.length;i++) {
					b.getCell(posPlayer1[i]).value = "X";
					b.getCell(posPlayer2[i]).value = "O";
				}
				assertThat(b.getCellsIfWinner("X")).isEqualTo(posPlayer1);
	}
	
	@Test
	public void testchekifcellswinner_Player2() {
				int[] posPlayer1 = { 1, 2, 7 };
				int[] posPlayer2 = { 0, 4, 8 };
				
				for(int i=0;i<posPlayer1.length;i++) {
					b.getCell(posPlayer1[i]).value = "X";
					b.getCell(posPlayer2[i]).value = "O";
				}
				assertThat(b.getCellsIfWinner("O")).isEqualTo(posPlayer2);
	}
	
	@Test
	public void testDraw() {
		int[] posPlayer1 = { 4, 7, 2, 3, 8};
		int[] posPlayer2 = { 5, 1, 6, 0};
		
		for(int i=0;i<posPlayer1.length;i++) {
			b.getCell(posPlayer1[i]).value = "X";
			if(i!=4) {
			b.getCell(posPlayer2[i]).value = "O";
			}
		}
		assertThat(b.checkDraw()).isEqualTo(true); 
	}
	
	@ParameterizedTest
	@MethodSource("values")
	public void TicTacToeGame_TestGana1(int argument) {
		TicTacToeGame game = new TicTacToeGame();
		Connection c0 = mock(Connection.class);
		Connection c1 = mock(Connection.class);	
		game.addConnection(c0);
		game.addConnection(c1);
		ArrayList<Player> jugadores = new ArrayList<Player>();
		jugadores.add(new Player(0, "Pepe", "X"));
				
		//añadimos el primer jugador a la partida y verificamos que se envian los mensajes
		game.addPlayer(jugadores.get(0));
		verify(c0).sendEvent(eq(EventType.JOIN_GAME), eq(jugadores));
		verify(c1).sendEvent(eq(EventType.JOIN_GAME), eq(jugadores));
		
		//añadimos otro jugador a la lista de jugadores, lo añadimos a la partida y verificamos
		jugadores.add(new Player(1, "Juan", "O"));
		game.addPlayer(jugadores.get(1));
		verify(c0, times(2)).sendEvent(eq(EventType.JOIN_GAME), eq(jugadores));
		verify(c1, times(2)).sendEvent(eq(EventType.JOIN_GAME), eq(jugadores));	
		//verifica que se pasa de turno
		verify(c0).sendEvent(eq(EventType.SET_TURN), eq(jugadores.get(0)));
		verify(c1).sendEvent(eq(EventType.SET_TURN), eq(jugadores.get(0)));
		ArrayList<Integer> mov1= new ArrayList<Integer>(argument);
	
		int siguienteTurno = 1;
		//Recorremos los movimientos que se producen
		for (int i=0;i<mov1.size();i++) {
			int TurnoActual = i;
			game.mark(mov1.get(i));
			ArgumentCaptor<CellMarkedValue> argumentoCelda = ArgumentCaptor.forClass(CellMarkedValue.class);
			verify(c0).sendEvent(eq(EventType.MARK), argumentoCelda.capture());
			assertThat(argumentoCelda.getValue().cellId).isEqualTo(mov1.get(i));
			assertThat(argumentoCelda.getValue().player.getId()).isEqualTo(TurnoActual);
			verify(c1).sendEvent(eq(EventType.MARK), argumentoCelda.capture());
			assertThat(argumentoCelda.getValue().cellId).isEqualTo(mov1.get(i) );
			assertThat(argumentoCelda.getValue().player.getId()).isEqualTo(TurnoActual);
			
			if (game.checkWinner()!=null) {
				ArgumentCaptor<WinnerValue> argument1 = ArgumentCaptor.forClass(WinnerValue.class);
				verify(c0).sendEvent(eq(EventType.GAME_OVER), argument1.capture());
				assertThat(argument1.getValue().pos).isNotEqualTo(null);
				assertThat(argument1.getValue().player.getId()).isEqualTo(TurnoActual);
				verify(c1).sendEvent(eq(EventType.GAME_OVER), argument1.capture());
				assertThat(argument1.getValue().pos).isNotEqualTo(null);
				assertThat(argument1.getValue().player.getId()).isEqualTo(TurnoActual);
			}
			else if (game.checkDraw()) {
				ArgumentCaptor<WinnerValue> argument1 = ArgumentCaptor.forClass(WinnerValue.class);
				verify(c0).sendEvent(eq(EventType.GAME_OVER), argument1.capture());
				assertThat(argument1.getValue()).isNull();
				verify(c1).sendEvent(eq(EventType.GAME_OVER), argument1.capture());
				assertThat(argument1.getValue()).isNull();
			}
			else {
				verify(c0).sendEvent(eq(EventType.SET_TURN), eq(jugadores.get(siguienteTurno)));
				verify(c1).sendEvent(eq(EventType.SET_TURN), eq(jugadores.get(siguienteTurno)));
			}
			siguienteTurno =siguienteTurno+1;
			reset(c0);
			reset(c1);
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
	
//********************************************************************************		
/*		public Collection<Object[]> movements= values();

	    private List<WebDriver> drivers = new ArrayList<WebDriver>();

	    @BeforeClass
	    public static void setupClass() {
	            WebDriverManager.chromedriver().setup();
	            Application.start();
	    }

	    @AfterClass
	    public static void teardownClass() {
	            Application.stop();
	    }

	    @Before
	    public void setupTest() {
	        drivers.add(new ChromeDriver());
	        drivers.add(new ChromeDriver());
	    }

	    @After
	    public void teardown() {
	    	for(WebDriver driver : drivers) {
	            if (driver != null) {
	                    driver.quit();
	            }
	    	}
	    	drivers.clear();
	    }
	    

		@Test
		public void TicTacToeWeb_Generic_System_Test() throws InterruptedException {
			for(WebDriver driver : drivers) {
				driver.get("http://localhost:8080/");
			}
			
			drivers.get(0).findElement(By.id("nickname")).sendKeys(movements.get(0).label);
			drivers.get(0).findElement(By.id("startBtn")).click();

			drivers.get(1).findElement(By.id("nickname")).sendKeys(movements.get(1).label);
			drivers.get(1).findElement(By.id("startBtn")).click();
			
			int nextTurn = 1;
			
			for (Movimientos movement : movements) {
				int thisTurn = nextTurn == 1 ? 0 : 1;
				drivers.get(thisTurn).findElement(By.id("cell-" + movement.pos)).click();
				
				if (movement.isWinner()) {
					
					//Wait for dialog
					Thread.sleep(500);
					
					assertThat(
							drivers.get(thisTurn).switchTo().alert().getText()
							).isEqualTo(
									movement.label + " wins! " + movements.get(nextTurn).label + " looses.");
				}
				else if (movement.isDraw()) {
					assertThat(
							drivers.get(thisTurn).switchTo().alert().getText()
							).isEqualTo("Draw!");
				}
				nextTurn = nextTurn == 1 ? 0 : 1;
			}
		}*/
	}