package net.mgsx.rainyday.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public class RDAssets {
	private static RDAssets i;
	public static RDAssets i(){
		return i == null ? i = new RDAssets() : i;
	}
	
	public Texture perlin, background;
	public ShaderProgram rainShader, skyShader, heroShader;
	public TextureRegion mushroomTexture, monsterTexture, pressStartTexture, titleTexture;
	
	private ObjectMap<String, TiledMap> maps = new ObjectMap<String, TiledMap>();
	public Texture textureHeroPlain;
	public Texture textureHeroSkeleton;
	
	public RDAssets() {
		perlin = new Texture(Gdx.files.internal("perlin.png"));
		perlin.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		background = new Texture(Gdx.files.internal("background.png"));
		background.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
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

		TiledMap baseMap = getMap("map-empty.tmx");
		mushroomTexture = baseMap.getTileSets().getTileSet(0).getTile(7).getTextureRegion();
		monsterTexture = baseMap.getTileSets().getTileSet(0).getTile(8).getTextureRegion();

		pressStartTexture = new TextureRegion(baseMap.getTileSets().getTileSet(0).getTile(1).getTextureRegion().getTexture(), 
				0, 6 * 32, 8 * 32, 2 * 32);
		titleTexture = new TextureRegion(baseMap.getTileSets().getTileSet(0).getTile(1).getTextureRegion().getTexture(), 
				0, 9 * 32, 16 * 32, 3 * 32);
		
		textureHeroPlain = new Texture(Gdx.files.internal("skeleton2.png"));
		textureHeroSkeleton = new Texture(Gdx.files.internal("skeleton3.png"));
	}
	
	public TiledMap getMap(String name){
		TiledMap map = maps.get(name);
		if(map == null){
			maps.put(name, map = new TmxMapLoader().load(name));
		}
		return map;
	}
}
