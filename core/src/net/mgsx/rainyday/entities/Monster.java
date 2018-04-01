package net.mgsx.rainyday.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Monster extends Entity
{
	private static enum State{
		APPEAR, MOVE, KILL, EVAPORATE, OVER
	}
	private Sprite sprite;
	private float time;
	private Hero hero;
	private Vector2 direction = new Vector2();
	private State state = State.APPEAR;
	private float scale;
	
	public Monster(Hero hero, TextureRegion region) {
		this.hero = hero;
		sprite = new Sprite(region);
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
	}
	
	@Override
	public void update(float delta) {
		switch(state){
		case APPEAR:{
				time += delta;
				if(time >= 1){
					time = 0;
					state = State.MOVE;
				}
			}
			break;
		case EVAPORATE:{
				time += delta * 1f;
				if(time >= 1){
					state = State.OVER;
				}
			}
			break;
		case KILL:{
				time += delta;
				if(time >= 1){
					state = State.OVER;
				}
			}
			break;
		case MOVE:{
				time += delta;
				direction.set(hero.position).add(0, 32).sub(position);
				float len = direction.len();
				if(len > 24){
					float speed = 30;
					position.mulAdd(direction, speed * delta / len);
				}else{
					hero.hurt();
					state = State.KILL;
					time = 0;
				}
			}
			break;
		case OVER:
			break;
		}
	}
	
	@Override
	public void draw(Batch batch){
		switch(state){
		case APPEAR:
			sprite.setScale(time);
			break;
		case EVAPORATE:
			sprite.setScale(MathUtils.lerp(scale, 5, time));
			sprite.setColor(1, 1, 1, 1 - time);
			break;
		case KILL:
			sprite.setScale(MathUtils.lerp(1f, 2f, Interpolation.sine.apply(4 * time)));
			break;
		case MOVE:
			sprite.setScale(MathUtils.lerp(1f, 1.5f, Interpolation.sine.apply(2 * time)));
			break;
		case OVER:
			return;
		}
		sprite.setPosition(position.x - sprite.getWidth()/2, position.y - sprite.getHeight()/2);
		sprite.draw(batch);
	}

	public boolean isOver() {
		return state == State.OVER;
	}

	public void hide() 
	{
		scale = sprite.getScaleX();
		state = State.EVAPORATE;
		time = 0;
	}

	
}