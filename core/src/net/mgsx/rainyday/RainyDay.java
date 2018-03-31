package net.mgsx.rainyday;

import com.badlogic.gdx.Game;

import net.mgsx.rainyday.screens.RDGameScreen;

public class RainyDay extends Game {
	
	
	@Override
	public void create () {
		// TODO create
		setScreen(new RDGameScreen());
	}
	
}
