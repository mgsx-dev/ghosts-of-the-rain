package net.mgsx.rainyday.screens;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import net.mgsx.rainyday.RainyDay;
import net.mgsx.rainyday.assets.RDAssets;
import net.mgsx.rainyday.entities.Entity;
import net.mgsx.rainyday.entities.Hero;
import net.mgsx.rainyday.entities.Monster;
import net.mgsx.rainyday.entities.Mushroom;
import net.mgsx.rainyday.utils.TiledMapLink;
import net.mgsx.rainyday.utils.TiledMapStream;

public class RDGameScreen extends RDBaseScreen
{
	private static final int SPAWN_TILE_EXTRA_WIDTH = 2;
	
	/** spawn window in tiles */
	private static final int SPAWN_TILE_WIDTH = 640 / 32 + 2 * SPAWN_TILE_EXTRA_WIDTH;
	
	private static final float SPAWN_CHANCE = .2f;
	
	private Hero hero;
	private float rainTime;
	private Array<Mushroom> mushrooms = new Array<Mushroom>();
	private Array<Monster> monsters = new Array<Monster>();
	private Array<Entity> entities = new Array<Entity>();
	private WorldState worldState = WorldState.SUMMER;
	private float worldStateTimeout = 5;

	private TiledMapTileLayer laddersLayer;
	
	public RDGameScreen() {
		super();
		
		hero = new Hero();
	}
	
	@SuppressWarnings("unused")
	@Override
	protected TiledMapStream createMapStream() 
	{
		TiledMap baseMap = RDAssets.i().getMap("map1.tmx");
		
		// map stream with a look ahead of 30 tiles (640 screen + 320 lookahead)
		TiledMapStream mapStream = new TiledMapStream(baseMap, 30);
		
		String [] names = new String[]{"map-start", "map1", "map-complex", "map-double", "map-desert", "map-libgdx"};
		String debugMap = null; // for testing purpose names[5];
		boolean randomSequence = true;
		
		TiledMapLink prev = null, first = null;
		if(debugMap != null){
			first = prev = mapStream.appendMap(RDAssets.i().getMap(debugMap + ".tmx"));
		}
		else if(randomSequence){
			Array<String> randName = new Array<String>();
			for(int i=1; i<names.length ; i++){
				randName.add(names[i]);
			}
			prev = mapStream.appendMap(RDAssets.i().getMap(names[0] + ".tmx"));
			while(randName.size > 0){
				TiledMapLink linkMap = mapStream.appendMap(RDAssets.i().getMap(randName.removeIndex(MathUtils.random(randName.size-1)) + ".tmx"));
				prev = linkMap;
				if(first == null){
					first = linkMap;
				}
			}
		}else{
			for(String name : names){
				// create a loop on the same map
				TiledMapLink linkMap = mapStream.appendMap(RDAssets.i().getMap(name + ".tmx"));
				prev = linkMap;
				if(first == null){
					first = prev;
				}
			}
		}
		prev.nextMap = first;
		
		laddersLayer = mapStream.getTileLayer("ladders");
		
		return mapStream;
	}
	
	@Override
	protected void update(float delta) {
		super.update(delta);
		
		if(hero.isDead()){
			if(hero.isOver()){
				RainyDay.i().backToMenu();
			}
		}
		
		rainTime += delta;
		rain = MathUtils.sinDeg(rainTime * 360 / 10f)*.5f+.5f;
		
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
		
		for(Mushroom mushroom : mushrooms){
			if(!mushroom.isHidden() && hero.position.dst(mushroom.position) < 16){
				hero.eat();
				mushroom.eat();
				mushroom.hide(0);
			}else{
				// replace mushroom if out of screen
				if(mushroom.position.x < cameraPosition.x - 320 - SPAWN_TILE_EXTRA_WIDTH * 32){
					int oldIX = (int)(mushroom.position.x / 32);
					int newIX = oldIX + SPAWN_TILE_WIDTH;
					int newIY = findGroundPosition(newIX);
					setEntityPosition(mushroom.position, newIX, newIY);
				}
			}
		}
		
		// chack rain
		if(rain >= .5f) 
		{
			boolean underRainLeft = true;
			{
				int ix = (int)((hero.position.x - 8) / 32f);
				int iy = (int)((hero.position.y + 56) / 32f);
				for( ; iy < 20 ; iy++){
					
					Cell cell = mapStream.getCell(groundLayer, ix, iy);
					if(cell != null && cell.getTile() != null){
						underRainLeft = false;
						break;
					}
				}
			}
			boolean underRainRight = true;
			{
				int ix = (int)((hero.position.x + 8) / 32f);
				int iy = (int)((hero.position.y + 56) / 32f);
				for( ; iy < 20 ; iy++){
					
					Cell cell = mapStream.getCell(groundLayer, ix, iy);
					if(cell != null && cell.getTile() != null){
						underRainRight = false;
						break;
					}
				}
			}
			hero.underRain = underRainLeft || underRainRight;
		}else{
			hero.underRain = false;
		}
		
		for(Entity entity : entities){
			entity.update(delta);
		}
		
		for(int i=0 ; i<mushrooms.size ; ){
			Mushroom mushroom = mushrooms.get(i);
			if(mushroom.isOver()){
				entities.removeValue(mushroom, true);
				mushrooms.removeIndex(i);
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
			
			Cell cell = mapStream.getCell(groundLayer, ix, iy);
			hero.canGoRight = (cell == null || cell.getTile() == null);
			if(!hero.canGoRight){
				//hero.position.x = (ix) * 32;
			}
		}
		// block on left obstacle
		{
			int ix = MathUtils.floor(hero.position.x / 32 - .5f);
			int iy = (int)(hero.position.y / 32 + 1);
			
			Cell cell = mapStream.getCell(groundLayer, ix, iy);
			hero.canGoLeft = (cell == null || cell.getTile() == null);
			if(!hero.canGoLeft){
				//hero.position.x = (ix) * 32;
			}
		}

		boolean onLadders = hero.canGoDown || hero.canGoUp;
		
		if(!onLadders){
			
			// falling
			for(;;){
				int ix = (int)(hero.position.x / 32);
				int iy = MathUtils.ceil(hero.position.y / 32 - .5f);
				
				Cell cell = mapStream.getCell(groundLayer, ix, iy);
				if(cell != null && cell.getTile() != null) break;
				if(hero.position.y < 0) break;
				hero.position.y -= 600 * delta;
				break;
			}
			// bubble up
			for(;;){
				int ix = (int)(hero.position.x / 32);
				int iy = MathUtils.floor(hero.position.y / 32 + 1f);
				
				Cell cell = mapStream.getCell(groundLayer, ix, iy);
				if(cell == null) break;
				if(cell.getTile() == null) break;
				
				hero.position.y += 32;
			}
			
		}
		
		
		{
			int ix = (int)(hero.position.x / 32);
			int iy = (int)(hero.position.y / 32 + .5f);
			Cell cell = mapStream.getCell(laddersLayer, ix, iy);
			if(cell != null && cell.getTile() != null){
				hero.canGoDown = true;
			}else{
				hero.canGoDown = false;
			}
		}
		{
			int ix = (int)(hero.position.x / 32);
			int iy = MathUtils.ceil(hero.position.y / 32 + .5f);
			Cell cell = mapStream.getCell(laddersLayer, ix, iy);
			if(cell != null && cell.getTile() != null){
				hero.canGoUp = true;
			}else{
				hero.canGoUp = false;
			}
		}
		
		boolean autoScroll = false; // XXX worldState != WorldState.SUMMER;
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
	}
	
	@Override
	protected void draw() 
	{
		drawSky();
		drawMap();
		drawMushrroms();
		drawHero();
		drawMonsters();
		drawRain();
	}
	
	private void drawMushrroms(){
		for(Mushroom mushroom : mushrooms){
			mushroom.draw(batch);
		}
	}
	
	private void drawHero(){
		ShaderProgram heroShader = RDAssets.i().heroShader;
		batch.setShader(heroShader);
		// hero.life = 99;
		heroShader.setUniformf("u_life", hero.life / Hero.MAX_LIFE);
		heroShader.setUniformf("u_y", hero.isDead() ? -10 : hero.position.y + 4 + 8); // TODO sprite based !
		heroShader.setUniformf("u_height", 46f);
		heroShader.setUniformi("u_texture2", 1);
		hero.draw(batch);
		batch.setShader(null);
	}
	private void drawMonsters(){
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		//heroShader.setUniformf("u_color", Color.PURPLE);
		for(Monster monster : monsters){
			monster.draw(batch);
		}
		batch.flush();
	}
	
	private int findGroundPosition(int ix) {
		for(int iy=24 ; iy>=0 ; iy--){
			Cell cell = mapStream.getCell(groundLayer, ix, iy);
			if(cell != null && cell.getTile() != null){
				return iy;
			}
		}
		return 0;
	}

	private void transformMonsters() {
		for(Monster monster : monsters){
			monster.hide(); 
		}
	}

	private void transformMushrooms() {
		for(Mushroom mushroom : mushrooms){
			mushroom.hide(MathUtils.random(1f)); 
			
			if(!mushroom.isEaten()){
				Monster monster = new Monster(hero);
				monster.position.set(mushroom.position).add(0, 32);
				monsters.add(monster);
				entities.add(monster);
			}
		}
	}

	private void spawnMushrooms() {
		// spawn some mushrooms in full world
		for(int i=0 ; i<SPAWN_TILE_WIDTH ; i++){
			
			if(!MathUtils.randomBoolean(SPAWN_CHANCE)) continue;
			
			int ix = (int)((camera.position.x - 320) / 32 ) + i;
			int iy = findGroundPosition(ix);
			
			Mushroom mushroom = new Mushroom();
			setEntityPosition(mushroom.position, ix, iy);
			mushrooms.add(mushroom);
			entities.add(mushroom);
		}
	}
	
	private void setEntityPosition(Vector2 position, int ix, int iy){
		position.x = (ix + .5f) * 32;
		position.y = (iy + .5f) * 32;
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false);
		viewport.update(width, height, true);
	}
}
