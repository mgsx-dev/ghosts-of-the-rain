package net.mgsx.rainyday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.rainyday.entities.Entity;
import net.mgsx.rainyday.entities.Hero;
import net.mgsx.rainyday.entities.Monster;
import net.mgsx.rainyday.entities.Mushroom;

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
	private Texture perlin;
	private ShaderProgram rainShader, skyShader, heroShader;
	private float time;
	private float [] randomLookup;
	private Color [] dryColors = new Color[]{new Color(), new Color()};
	private Color [] wetColors = new Color[]{new Color(), new Color()};
	private Color [] skyColors = new Color[]{new Color(), new Color()};
	private float rainTime;
	private TextureRegion mushroomTexture, monsterTexture;
	private Array<Mushroom> mushrooms = new Array<Mushroom>();
	private Array<Monster> monsters = new Array<Monster>();
	private Array<Entity> entities = new Array<Entity>();
	
	private static enum WorldState{
		WINTER, SPRING, SUMMER, AUTUMN, 
	}
	private WorldState worldState = WorldState.WINTER;
	private float worldStateTimeout;
	private TiledMapTileLayer groundLayer;
	
	public RDGameScreen() {
		map = new TmxMapLoader().load("map1.tmx");
		groundLayer = (TiledMapTileLayer)map.getLayers().get("ground");
		mushroomTexture = map.getTileSets().getTileSet(0).getTile(7).getTextureRegion();
		monsterTexture = map.getTileSets().getTileSet(0).getTile(8).getTextureRegion();
		camera = new OrthographicCamera(640, 480);
		viewport = new FitViewport(640, 480, camera);
		batch = new SpriteBatch();
		mapRenderer = new OrthogonalTiledMapRenderer(map, batch);
		hero = new Hero();
		shapeRenderer = new ShapeRenderer();
		perlin = new Texture(Gdx.files.internal("perlin.png"));
		rainShader = new ShaderProgram(Gdx.files.internal("shaders/rain.vs"), Gdx.files.internal("shaders/rain.fs"));
		if(!rainShader.isCompiled()){
			throw new GdxRuntimeException(rainShader.getLog());
		}
		skyShader = new ShaderProgram(Gdx.files.internal("shaders/sky.vs"), Gdx.files.internal("shaders/sky.fs"));
		if(!skyShader.isCompiled()){
			throw new GdxRuntimeException(skyShader.getLog());
		}
		heroShader = new ShaderProgram(Gdx.files.internal("shaders/hero.vs"), Gdx.files.internal("shaders/hero.fs"));
		if(!heroShader.isCompiled()){
			throw new GdxRuntimeException(heroShader.getLog());
		}
		
		perlin.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		randomLookup = new float[256];
		for(int i=0 ; i<randomLookup.length ; i++) randomLookup[i] = MathUtils.random();
	}
	
	@Override
	public void render(float delta) {
		
		// update
		
		time += delta * 3;
		
		rainTime += delta;
		
		float rain = MathUtils.sinDeg(rainTime * 360 / 10f)*.5f+.5f;
		
		worldStateTimeout -= delta;
		if(worldState == WorldState.WINTER){
			rain = 1;
			if(worldStateTimeout <= 0){
				worldStateTimeout = 1;
				worldState = WorldState.SPRING;
				transformMonsters();
			}
		}else if(worldState == WorldState.SPRING){
			rain = worldStateTimeout;
			if(worldStateTimeout <= 0){
				worldStateTimeout = 5;
				worldState = WorldState.SUMMER;
				rain = 0;
				spawnMushrooms();
			}
		}else if(worldState == WorldState.SUMMER){
			rain = 0;
			if(worldStateTimeout <= 0){
				worldStateTimeout = 1;
				worldState = WorldState.AUTUMN;
				transformMushrooms();
			}
		}else if(worldState == WorldState.AUTUMN){
			rain = 1 - worldStateTimeout;
			if(worldStateTimeout <= 0){
				worldStateTimeout = 5;
				worldState = WorldState.WINTER;
				rain = 1;
			}
		}
		
		
		hero.update(delta);
		
		for(Entity entity : entities){
			entity.update(delta);
		}
		
		for(int i=0 ; i<mushrooms.size ; ){
			Mushroom mushroom = mushrooms.get(i);
			if(mushroom.isOver()){
				entities.removeValue(mushroom, true);
				mushrooms.removeIndex(i);
				
				Monster monster = new Monster(hero, monsterTexture);
				monster.position.set(mushroom.position);
				monsters.add(monster);
				entities.add(monster);
			}else{
				i++;
			}
		}
		for(int i=0 ; i<monsters.size ; ){
			Monster monster = monsters.get(i);
			if(monster.isOver()){
				entities.removeValue(monster, true);
				monsters.removeIndex(i);
			}else{
				i++;
			}
		}
		
		
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
		
		boolean autoScroll = worldState != WorldState.SUMMER;
		if(autoScroll)
		{
			cameraPosition.x += delta * 40; // XXX
		}else{
			if(cameraPosition.x < hero.position.x){
				
				cameraPosition.x = MathUtils.lerp(cameraPosition.x, hero.position.x, delta);
			}
		}
		hero.minX = cameraPosition.x - 320 + 32;
		hero.maxX = cameraPosition.x + 320 - 32;
		camera.position.set(cameraPosition, 0);
		
		// render
		
		camera.update();
		
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		dryColors[0].set(.5f, .7f, .95f, 1);
		dryColors[1].set(.9f, .9f, .9f, 1);
		
		wetColors[0].set(.3f, .3f, .3f, 1);
		wetColors[1].set(.5f, .4f, .3f, 1);
		
		skyColors[0].set(dryColors[0]).lerp(wetColors[0], rain);
		skyColors[1].set(dryColors[1]).lerp(wetColors[1], rain);
		
		batch.disableBlending();
		batch.setShader(skyShader);
		skyShader.setUniformf("u_color_sky", skyColors[0]);
		skyShader.setUniformf("u_color_horizon", skyColors[1]);
		batch.draw(perlin, camera.position.x - 320, 0, 640, 480);
		batch.setShader(null);
		batch.enableBlending();
		
		batch.setShader(heroShader);
		
		heroShader.setUniformf("u_color", Color.GREEN);
		batch.end();
		mapRenderer.setView(camera);
		mapRenderer.render();
		batch.begin();
		
		heroShader.setUniformf("u_color", Color.ORANGE);
		hero.draw(batch);
		batch.flush();
		
		heroShader.setUniformf("u_color", Color.BROWN);
		for(Mushroom mushroom : mushrooms){
			mushroom.draw(batch);
		}
		batch.flush();
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		heroShader.setUniformf("u_color", Color.PURPLE);
		for(Monster monster : monsters){
			monster.draw(batch);
		}
		batch.flush();
		
		batch.setShader(null);
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		batch.setShader(rainShader);
		rainShader.setUniformf("u_time", time);
		int NRAYS = 21;
		float rayWidth = 32;
		for(int i=0 ; i<NRAYS ; i++){
			int ix = (int)(camera.position.x / 32 + i - NRAYS/2);
			int piy = 0;
			for(int iy=24 ; iy>=0 ; iy--){
				Cell cell = groundLayer.getCell(ix, iy);
				if(cell != null && cell.getTile() != null){
					piy = iy;
					break;
				}
			}
			float fx = (ix + .5f) * 32;
			float fy2 = (piy + .5f) * 32;
			// XXX fy2 = MathUtils.lerp(640, fy2, MathUtils.clamp(rain * 1, 0, 1));
			if(Math.abs(hero.position.x - fx) < 32){
				fy2 = Math.max(fy2, hero.position.y + 64 - 16);
			}
			
			batch.setColor(randomLookup[ix % randomLookup.length], 1,1, rain * 0.3f);
			batch.draw(perlin, fx - rayWidth/2, fy2, rayWidth, 640);
		}
		batch.setShader(null);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		
		boolean debugHero = false;
		if(debugHero){
			
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.circle(hero.position.x, hero.position.y, 32);
		}
		
		boolean debugRays = false;
		if(debugRays){
			shapeRenderer.setColor(Color.BLUE);
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
		}
		
		
		
		
		shapeRenderer.end();
	}
	
	private void transformMonsters() {
		for(Monster monster : monsters){
			monster.hide(MathUtils.random(1f)); 
		}
	}

	private void transformMushrooms() {
		for(Mushroom mushroom : mushrooms){
			mushroom.hide(MathUtils.random(1f)); 
		}
	}

	private void spawnMushrooms() {
		int NMUSHROOMS = 5;
		for(int i=0 ; i<NMUSHROOMS ; i++){
			int ix = (int)((camera.position.x + MathUtils.random(640f) - 320) / 32 );
			int piy = -1;
			for(int iy=24 ; iy>=0 ; iy--){
				Cell cell = groundLayer.getCell(ix, iy);
				if(cell != null && cell.getTile() != null){
					piy = iy;
					break;
				}
			}
			if(piy <= 0) continue;
			float fx = (ix + .5f) * 32;
			float fy2 = (piy + .5f) * 32;
			
			Mushroom mushroom = new Mushroom(mushroomTexture);
			mushroom.position.set(fx, fy2);
			mushrooms.add(mushroom);
			entities.add(mushroom);
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false);
		viewport.update(width, height, true);
	}
}
