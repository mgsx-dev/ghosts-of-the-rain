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

	public float minX;

	
	public Hero() {
		texture = new Texture(Gdx.files.internal("skeleton.png"));
		
		sprite = new Sprite(texture, 0, 0, 64, 64);
	}
	
	public void draw(Batch batch){
		sprite.draw(batch);
	}

	public void update(float delta) 
	{
		float dx = 0;
		if(canGoLeft && Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			dx = -1;
			sprite.setRegion(0, 64 * 1, 64, 64);
		}
		if(canGoRight && Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			dx = 1;
			sprite.setRegion(0, 64 * 3, 64, 64);
		}
		float dy = 0;
		if(canGoUp && Gdx.input.isKeyPressed(Input.Keys.UP)){
			dy = 1;
		}
		if(canGoDown && Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			dy = -1;
		}
		
		float speed = 200;
		
		if(position.x < minX) position.x = minX;
		
		position.add(dx * delta * speed, dy * delta * speed);
		
		sprite.setPosition(position.x - 32, position.y);
		
	}
}
