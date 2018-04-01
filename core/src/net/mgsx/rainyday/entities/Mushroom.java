package net.mgsx.rainyday.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;

public class Mushroom extends Entity
{

	private Sprite sprite;
	private float time;
	private boolean hide;
	private float hideTimeout;
	
	public Mushroom(TextureRegion region) {
		sprite = new Sprite(region);
		sprite.setOrigin(sprite.getWidth()/2, 0);
	}
	
	@Override
	public void update(float delta) {
		time += delta;
		if(hide){
			hideTimeout -= delta;
		}
	}
	
	@Override
	public void draw(Batch batch){
		if(hide){
			if(hideTimeout < 0)
				sprite.setScale(Interpolation.bounceOut.apply(1 - time));
		}else{
			sprite.setScale(Interpolation.bounceOut.apply(time));
		}
		sprite.setPosition(position.x - sprite.getWidth()/2, position.y);
		sprite.draw(batch);
	}

	public boolean isOver() {
		return hide && time > 1;
	}

	public void hide(float timeout) 
	{
		hide = true;
		hideTimeout = timeout;
		time = 0;
	}

	
}
