package net.mgsx.rainyday.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;

import net.mgsx.rainyday.assets.RDAssets;

public class Mushroom extends Entity
{

	private Sprite sprite;
	private float time;
	private boolean hide;
	private float hideTimeout;
	private boolean eaten = false;
	
	public Mushroom() {
		sprite = new Sprite(RDAssets.i().mushroomTexture);
		sprite.setOrigin(sprite.getWidth()/2, 0);
	}
	
	@Override
	public void update(float delta) {
		time += delta * (eaten ? 2 : 1);
		if(hide){
			hideTimeout -= delta;
		}
	}
	
	@Override
	public void draw(Batch batch){
		float baseScale = 1f;
		if(hide){
			if(hideTimeout < 0)
				sprite.setScale(baseScale * Interpolation.bounceOut.apply(1 - time));
		}else{
			sprite.setScale(baseScale * Interpolation.bounceOut.apply(time));
		}
		sprite.setPosition(position.x - sprite.getWidth()/2, position.y + sprite.getHeight()/4);
		sprite.draw(batch);
	}

	public boolean isOver() {
		return hide && time > 1;
	}
	
	public boolean isHidden() {
		return hide;
	}
	
	public void eat(){
		eaten = true;
	}
	
	public boolean isEaten() {
		return eaten;
	}

	public void hide(float timeout) 
	{
		hide = true;
		hideTimeout = timeout;
		time = 0;
	}
	
}
