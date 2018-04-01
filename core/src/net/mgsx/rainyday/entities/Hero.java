package net.mgsx.rainyday.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Hero {

	private Texture texture;
	
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
	
	public Hero() {
		texture = new Texture(Gdx.files.internal("skeleton2.png"));
		
		sprite = new Sprite(texture, 0, 0, 64, 64);
	}
	
	public void draw(Batch batch){
		sprite.draw(batch);
	}

	public void update(float delta) 
	{
		float animationSpeed = 20;
		float dx = 0;
		float dy = 0;
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
		
		float speed = 200;
		
		if(position.x < minX) position.x = minX;
		if(position.x > maxX) position.x = maxX;
		
		position.add(dx * delta * speed, dy * delta * speed);
		
		sprite.setPosition(position.x - 32, position.y);
		
	}
}
