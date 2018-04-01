package net.mgsx.rainyday.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Hero {

	private Texture texturePlain, textureSkeleton;
	
	private Sprite sprite;

	public Vector2 position = new Vector2();

	public boolean canGoUp;
	public boolean canGoDown;
	public boolean canGoRight;
	public boolean canGoLeft;
	public boolean leftToRight = true;

	public float minX;

	private float animationTime;

	public float maxX;

	private float eatTimeout;
	
	public boolean asSkeleton = false;
	
	public static float MAX_LIFE = 100;
	public static float LIFE_PER_MUSHROOM = 20;
	public static float LIFE_PER_MONSTER = 10;
	public static float RAIN_DAMAGES_PER_SEC = 20;
	public float life = MAX_LIFE;

	private float hurtTimeout;
	private boolean dead;
	
	private Color color = new Color();

	public boolean underRain;

	private float flashTime;
	
	public Hero() {
		texturePlain = new Texture(Gdx.files.internal("skeleton2.png"));
		textureSkeleton = new Texture(Gdx.files.internal("skeleton3.png"));
		
		sprite = new Sprite(texturePlain, 0, 0, 64, 64);
	}
	
	public void draw(Batch batch){
		float l = life / MAX_LIFE;
		
		
		
		float flashFrequency = 8;
		float flashRatio = .5f;
		if(hurtTimeout > 0){
			if(((flashTime * flashFrequency) % 1f > flashRatio)){
				color.set(Color.WHITE);
			}else{
				color.set(Color.BLACK);
			}
			sprite.setTexture(textureSkeleton);
		}else{
			color.set(Color.WHITE).lerp(Color.RED, l);
			sprite.setTexture(texturePlain);
		}
		
		sprite.setColor(color);
		// sprite.setTexture(asSkeleton ? textureSkeleton : texturePlain);
		sprite.draw(batch);
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
		
		if(eatTimeout > 0){
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
		
		sprite.setPosition(position.x - 32, position.y);
		
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
		life -= damages;
		if(life < 0){
			dead = true;
			life = 0;
		}
		hurtTimeout = .3f;
	}
}
