package net.mgsx.rainyday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import net.mgsx.rainyday.screens.RDGameScreen;
import net.mgsx.rainyday.screens.RDMenuScreen;

public class RainyDay extends Game {
	
	public static RainyDay i(){
		return (RainyDay)Gdx.app.getApplicationListener();
	}
	
	private RDGameScreen gameScreen;
	private RDMenuScreen menuScreen;
	
	@Override
	public void create () {
		menuScreen = new RDMenuScreen();
		setScreen(menuScreen);
		// setScreen(new RDGameScreen());
	}

	public void startGame() 
	{
		if(gameScreen != null){
			gameScreen.dispose();
		}
		gameScreen = new RDGameScreen();
		setScreen(gameScreen);
	}
	
	public void backToMenu(){
		menuScreen.reset();
		setScreen(menuScreen);
	}
	
	
	
}
