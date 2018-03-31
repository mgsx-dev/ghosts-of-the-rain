package net.mgsx.rainyday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.rainyday.entities.Hero;

public class RDGameScreen extends ScreenAdapter
{
	
	private OrthographicCamera camera;
	private Viewport viewport;
	private TiledMapRenderer mapRenderer;
	private TiledMap map;
	private SpriteBatch batch;
	private Hero hero;
	private Vector2 cameraPosition = new Vector2(320, 240);
	private ShapeRenderer shapeRenderer;
	
	public RDGameScreen() {
		map = new TmxMapLoader().load("map1.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map);
		camera = new OrthographicCamera(640, 480);
		viewport = new FitViewport(640, 480, camera);
		batch = new SpriteBatch();
		hero = new Hero();
		shapeRenderer = new ShapeRenderer();
	}
	
	@Override
	public void render(float delta) {
		
		// update
		
		hero.update(delta);
		
		TiledMapTileLayer groundLayer = (TiledMapTileLayer)map.getLayers().get("ground");
		
		hero.canGoLeft = true;
		hero.canGoRight = true;
		
		// block on right obstacle
		{
			int ix = MathUtils.floor(hero.position.x / 32 + .5f);
			int iy = (int)(hero.position.y / 32 + 1);
			
			Cell cell = groundLayer.getCell(ix, iy);
			hero.canGoRight = (cell == null || cell.getTile() == null);
			if(!hero.canGoRight){
				//hero.position.x = (ix) * 32;
			}
		}
		// block on left obstacle
		{
			int ix = MathUtils.floor(hero.position.x / 32 - .5f);
			int iy = (int)(hero.position.y / 32 + 1);
			
			Cell cell = groundLayer.getCell(ix, iy);
			hero.canGoLeft = (cell == null || cell.getTile() == null);
			if(!hero.canGoLeft){
				//hero.position.x = (ix) * 32;
			}
		}

// XXX prevent falling
//		{
//			int ix = MathUtils.floor(hero.position.x / 32 + .5f);
//			int iy = (int)(hero.position.y / 32);
//			
//			Cell cell = groundLayer.getCell(ix, iy);
//			hero.canGoRight = (cell != null && cell.getTile() != null);
//			if(!hero.canGoRight){
//				//hero.position.x = (ix) * 32;
//			}
//		}
		
//		{
//			int ix = (int)(hero.position.x / 32 - .5f);
//			int iy = (int)(hero.position.y / 32 + 1);
//			
//			Cell cell = groundLayer.getCell(ix, iy);
//			hero.canGoLeft = (cell == null || cell.getTile() == null);
//		}
		
		boolean onLadders = hero.canGoDown || hero.canGoUp;
		
		if(!onLadders){
			
			// falling
			for(;;){
				int ix = (int)(hero.position.x / 32);
				int iy = MathUtils.ceil(hero.position.y / 32 - .5f);
				
				Cell cell = groundLayer.getCell(ix, iy);
				if(cell != null && cell.getTile() != null) break;
				if(hero.position.y < 0) break;
				hero.position.y -= 600 * delta;
				break;
			}
			// bubble up
			for(;;){
				int ix = (int)(hero.position.x / 32);
				int iy = MathUtils.floor(hero.position.y / 32 + 1f);
				
				Cell cell = groundLayer.getCell(ix, iy);
				if(cell == null) break;
				if(cell.getTile() == null) break;
				
				hero.position.y += 32;
			}
			
		}
		
		
		
		TiledMapTileLayer laddersLayer = (TiledMapTileLayer)map.getLayers().get("ladders");
		{
			int ix = (int)(hero.position.x / 32);
			int iy = (int)(hero.position.y / 32 - .5f);
			Cell cell = laddersLayer.getCell(ix, iy);
			if(cell != null && cell.getTile() != null){
				hero.canGoDown = true;
			}else{
				hero.canGoDown = false;
			}
		}
		{
			int ix = (int)(hero.position.x / 32);
			int iy = MathUtils.ceil(hero.position.y / 32 - .5f);
			Cell cell = laddersLayer.getCell(ix, iy);
			if(cell != null && cell.getTile() != null){
				hero.canGoUp = true;
			}else{
				hero.canGoUp = false;
			}
		}
		
		if(camera.position.x < hero.position.x){
			
			cameraPosition.x = MathUtils.lerp(camera.position.x, hero.position.x, delta);
		}
		hero.minX = camera.position.x - 320 + 32;
		camera.position.set(cameraPosition, 0);
		
		// render
		
		camera.update();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		mapRenderer.setView(camera);
		mapRenderer.render();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		hero.draw(batch);
		batch.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.circle(hero.position.x, hero.position.y, 32);
		
		shapeRenderer.setColor(Color.BLUE);
		int NRAYS = 40;
		for(int i=0 ; i<NRAYS ; i++){
			int ix = (int)(camera.position.x / 32 + i * 20f / NRAYS - 10);
			int piy = 0;
			for(int iy=24 ; iy>=0 ; iy--){
				Cell cell = groundLayer.getCell(ix, iy);
				if(cell != null && cell.getTile() != null){
					piy = iy;
					break;
				}
			}
			float fx = (ix + .5f) * 32;
			float fy1 = 640;
			float fy2 = (piy + .5f) * 32;
			shapeRenderer.line(fx, fy1, fx, fy2);
		}
		
		
		
		
		shapeRenderer.end();
	}
	
	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false);
		viewport.update(width, height, true);
	}
}
