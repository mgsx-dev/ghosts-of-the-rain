package net.mgsx.rainyday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

import net.mgsx.rainyday.RainyDay;
import net.mgsx.rainyday.assets.RDAssets;
import net.mgsx.rainyday.utils.TiledMapLink;
import net.mgsx.rainyday.utils.TiledMapStream;

public class RDMenuScreen extends RDBaseScreen
{
	private boolean flashing;
	
	@Override
	protected TiledMapStream createMapStream() {
		
		// map stream with a look ahead of 30 tiles (640 screen + 320 lookahead)
		TiledMap titleMap = RDAssets.i().getMap("map-font.tmx");
		
		TiledMapStream mapStream = new TiledMapStream(titleMap, 30);
		
		// create a loop on the same map
		TiledMapLink linkMap = mapStream.appendMap(titleMap);
		linkMap.nextMap = linkMap;
		
		return mapStream;
	}
	
	@Override
	protected void update(float delta) {
		super.update(delta);
		rain = MathUtils.lerp(.9f, 1f, Interpolation.sine.apply(time * .05f));
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			RainyDay.i().startGame();
		}
		
		cameraPosition.x += delta * 100;
		
		if(delta > 0) flashing = MathUtils.randomBoolean(.05f);
	}
	
	@Override
	protected void draw() {
		super.draw();
		
		TextureRegion region;
		
		region = RDAssets.i().pressStartTexture;
		batch.setColor(1, 0, 0, MathUtils.lerp(.1f, 1f, Interpolation.sine.apply(time * 1f)));
		batch.draw(region, cameraPosition.x - region.getRegionWidth()/2, 32);
		
		region = RDAssets.i().titleTexture;
		float l = flashing ? 1 : 0;
		batch.setColor(l,l,l, 1);
		batch.draw(region, cameraPosition.x - region.getRegionWidth()/2, 480 - region.getRegionHeight());
	}
}
