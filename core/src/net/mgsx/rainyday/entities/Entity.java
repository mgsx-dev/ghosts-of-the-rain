package net.mgsx.rainyday.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

abstract public class Entity {
	public Vector2 position = new Vector2();
	
	abstract public void update(float delta);
	abstract public void draw(Batch batch);
}
