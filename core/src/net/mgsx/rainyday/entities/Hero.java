package net.mgsx.rainyday.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import net.mgsx.rainyday.assets.RDAssets;

public class Hero {

	private Sprite sprite;

	public Vector2 position = new Vector2(0, 480);

	public boolean canGoUp;
	public boolean canGoDown;
	public boolean canGoRight;
	public boolean canGoLeft;
	public boolean leftToRight = true;

	public float minX;

	private float animationTime;

	public float maxX;

	private float eatTimeout;
	
	public static float MAX_LIFE = 100;
	public static float LIFE_PER_MUSHROOM = 10;
	public static float LIFE_PER_MONSTER = 30;
	public static float RAIN_DAMAGES_PER_SEC = 20;
	public float life = MAX_LIFE;

	private float hurtTimeout;
	private boolean dead;
	
	private Color color = new Color();

	public boolean underRain;
	private boolean over;

	private float flashTime;
	
	public Hero() {
		sprite = new Sprite(RDAssets.i().textureHeroPlain, 0, 0, 64, 64);
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void draw(Batch batch){
		float l = life / MAX_LIFE;
		
		sprite.setPosition(position.x - 32, position.y + 8);
		
		float flashFrequency = 8;
		float flashRatio = .5f;
		if(hurtTimeout > 0){
			if(((flashTime * flashFrequency) % 1f > flashRatio)){
				color.set(Color.WHITE);
			}else{
				color.set(Color.BLACK);
			}
			sprite.setTexture(RDAssets.i().textureHeroSkeleton);
		}else{
			color.set(Color.WHITE).lerp(Color.RED, l);
			sprite.setTexture(RDAssets.i().textureHeroPlain);
		}
		sprite.setTexture(RDAssets.i().textureHeroPlain);
		RDAssets.i().textureHeroSkeleton.bind(1);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		sprite.setColor(color);
		sprite.draw(batch);
	}
	
	public boolean isOver() {
		return over;
	}

	public void update(float delta) 
	{
		if(underRain){
			hurt(delta * RAIN_DAMAGES_PER_SEC);
		}
		if(hurtTimeout > 0){
			hurtTimeout -= delta;
			flashTime += delta;
		}else{
			flashTime = 0;
		}
		
		float animationSpeed = 20;
		float dx = 0;
		float dy = 0;
		
		if(dead){
			animationTime += delta;
			sprite.setRegion((int)Math.min(4, animationTime * 4) * 64, 64 * 20, 64, 64);
			over = animationTime > 5;
		}
		else if(eatTimeout > 0){
			eatTimeout -= delta;
			animationTime += delta * animationSpeed;
			
			sprite.setRegion(((int)animationTime % 6) * 64, 64 * 12, 64, 64);
		}else{
			
			if(canGoLeft && Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				dx = -1;
				leftToRight = false;
				animationTime += delta * animationSpeed;
				sprite.setRegion(((int)animationTime % 9) * 64, 64 * 9, 64, 64);
			}
			else if(canGoRight && Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				dx = 1;
				leftToRight = true;
				animationTime += delta * animationSpeed;
				sprite.setRegion(((int)animationTime % 9) * 64, 64 * 11, 64, 64);
			}
			else if(canGoUp && Gdx.input.isKeyPressed(Input.Keys.UP)){
				animationTime += delta * animationSpeed;
				sprite.setRegion(((int)animationTime % 9) * 64, 64 * 8, 64, 64);
				dy = 1;
			}
			else if(canGoDown && Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				animationTime += delta * animationSpeed;
				sprite.setRegion(((int)animationTime % 9) * 64, 64 * 8, 64, 64);
				dy = -1;
			}
			else if(!canGoDown && !canGoUp){
				animationTime = 0;
				sprite.setRegion(((int)animationTime % 9) * 64, 64 * (leftToRight ? 11 : 9), 64, 64);
			}
		}
		
		float speed = 200; // XXX 200
		
		if(position.x < minX) position.x = minX;
		if(position.x > maxX) position.x = maxX;
		
		position.add(dx * delta * speed, dy * delta * speed);
	}

	public void eat() {
		life = Math.min(MAX_LIFE, life + LIFE_PER_MUSHROOM);
		eatTimeout = .3f;
		animationTime = 0;
	}
	public void hurt() {
		hurt(LIFE_PER_MONSTER);
	}
	private void hurt(float damages) {
		if(dead) return;
		life -= damages;
		if(life < 0){
			dead = true;
			life = 0;
			animationTime = 0;
		}
		hurtTimeout = .3f;
	}
}
