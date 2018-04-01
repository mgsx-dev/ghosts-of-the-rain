package net.mgsx.rainyday.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.rainyday.assets.RDAssets;
import net.mgsx.rainyday.utils.TiledMapStream;

abstract public class RDBaseScreen extends ScreenAdapter
{
	private static final boolean DEBUG = false;
	
	protected OrthographicCamera camera;
	protected Viewport viewport;
	protected OrthogonalTiledMapRenderer mapRenderer;
	protected SpriteBatch batch;
	protected Vector2 cameraPosition = new Vector2(320, 240);
	
	
	protected float time;
	protected Color [] dryColors = new Color[]{new Color(), new Color(), new Color()};
	protected Color [] wetColors = new Color[]{new Color(), new Color(), new Color()};
	protected Color [] skyColors = new Color[]{new Color(), new Color(), new Color()};
	protected Color rainColor = new Color();
	protected boolean paused;
	protected TiledMapStream mapStream;
	protected TiledMapTileLayer groundLayer;
	protected float rain = 1;
	protected float [] randomLookup;
	
	public RDBaseScreen() {
		mapStream = createMapStream();
		
		groundLayer = mapStream.getTileLayer("ground");
		
		camera = new OrthographicCamera(640, 480);
		viewport = new FitViewport(640, 480, camera);
		batch = new SpriteBatch();
		mapRenderer = new OrthogonalTiledMapRenderer(mapStream.getMap(), batch);
		
		randomLookup = new float[256];
		for(int i=0 ; i<randomLookup.length ; i++) randomLookup[i] = MathUtils.random();
		
	}
	
	abstract protected TiledMapStream createMapStream();
	
	@Override
	public void render(float delta) {
		update(delta);
		
		// render
		
		camera.position.set(cameraPosition, 0);
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		draw();
		
		batch.end();
		
	}
	
	protected void draw(){
		drawSky();
		drawMap();
		drawRain();
	}
	
	protected void update(float delta){
		if(DEBUG){
			if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
				paused = !paused;
			}
			if(paused){
				delta = 0;
			}
		}
		
		// update
		
		mapStream.update(cameraPosition.x - 320);
		
		time += delta * 3;
	}
	
	protected void drawMap(){
		
		batch.end();
		
		float lum = 1 - rain;
		batch.setColor(lum, lum, lum, 1);

		mapStream.begin(camera);
		mapRenderer.setView(camera);
		mapRenderer.render();
		mapStream.end(camera);
		
		// set batch matrix again because camera has changed
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
	}
	
	protected void drawSky(){
		dryColors[0].set(.5f, .7f, .95f, 1);
		dryColors[1].set(.9f, .9f, .9f, 1);
		dryColors[2].set(1f, 1f, 1f, 1);
		
		wetColors[0].set(.3f, .3f, .3f, 1);
		wetColors[1].set(.5f, .4f, .3f, 1);
		wetColors[2].set(.3f, .3f, .3f, .03f);
		
		skyColors[0].set(dryColors[0]).lerp(wetColors[0], rain);
		skyColors[1].set(dryColors[1]).lerp(wetColors[1], rain);
		skyColors[2].set(dryColors[2]).lerp(wetColors[2], rain);
		
		ShaderProgram skyShader = RDAssets.i().skyShader;
		batch.disableBlending();
		batch.setShader(skyShader);
		skyShader.setUniformf("u_color_sky", skyColors[0]);
		skyShader.setUniformf("u_color_horizon", skyColors[1]);
		skyShader.setUniformf("u_bg_color", skyColors[2]);
		skyShader.setUniformf("u_parallax", .5f * cameraPosition.x / 640f);
		batch.draw(RDAssets.i().background, camera.position.x - 320, 0, 640, 480);
		batch.setShader(null);
		batch.enableBlending();
	}
	
	protected void drawRain()
	{
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		
		
		rainColor.set(0, .5f, 1, 1);
		
		ShaderProgram rainShader = RDAssets.i().rainShader;
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		batch.setShader(rainShader);
		rainShader.setUniformf("u_time", time);
		rainShader.setUniformf("u_color", rainColor);
		int NRAYS = 21;
		float rayWidth = 32;
		for(int i=0 ; i<NRAYS ; i++){
			int ix = (int)(camera.position.x / 32 + i - NRAYS/2);
			int piy = 0;
			for(int iy=24 ; iy>=0 ; iy--){
				Cell cell = mapStream.getCell(groundLayer, ix, iy);
				if(cell != null && cell.getTile() != null){
					piy = iy;
					break;
				}
			}
			float fx = (ix + .5f) * 32;
			float fy2 = (piy + .75f) * 32;
			
			batch.setColor(randomLookup[ix % randomLookup.length], 1,1, rain * 1f);
			batch.draw(RDAssets.i().perlin, fx - rayWidth/2, fy2, rayWidth, 640);
		}
		batch.setShader(null);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false);
		viewport.update(width, height, true);
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		mapRenderer.dispose();
		super.dispose();
	}
}
