package net.mgsx.rainyday.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;

public class Monster extends Entity
{

	private Sprite sprite;
	private float time;
	private boolean hide;
	private float hideTimeout;
	private Hero hero;
	
	public Monster(Hero hero, TextureRegion region) {
		this.hero = hero;
		sprite = new Sprite(region);
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
	}
	
	@Override
	public void update(float delta) {
		time += delta;
		if(hide){
			hideTimeout -= delta;
		}
		position.lerp(hero.position, delta);
	}
	
	@Override
	public void draw(Batch batch){
		if(hide){
			if(hideTimeout < 0)
				sprite.setScale(Interpolation.bounceOut.apply(1 - time));
		}else{
			sprite.setScale(Interpolation.bounceOut.apply(time));
		}
		sprite.setPosition(position.x - sprite.getWidth()/2, position.y - sprite.getHeight()/2);
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