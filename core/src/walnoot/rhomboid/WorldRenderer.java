package walnoot.rhomboid;

import walnoot.libgdxutils.RenderContext;
import walnoot.rhomboid.components.PlayerComponent;
import walnoot.rhomboid.components.SpritesComponent;
import walnoot.rhomboid.components.SpritesComponent.ComponentSprite;
import walnoot.rhomboid.states.GameState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;

public class WorldRenderer {
	private static final float SECTOR_SIZE = 40f;
	
	private final GameWorld world;
	
	private float[] randoms = new float[73];
	
	private Box2DDebugRenderer debug = new Box2DDebugRenderer();
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
//	private RayHandler rays;
//	private BlurEffect blurEffect;
	
	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera camera = new OrthographicCamera();
	private OrthographicCamera uiCamera = new OrthographicCamera();

	private Array<ComponentSprite> componentSprites = new Array<>();
	
	private Entity player;

	private RenderContext renderContext;
	
	public WorldRenderer(GameWorld world, RenderContext renderContext) {
		this.world = world;
		this.renderContext = renderContext;
		
//		rays = new RayHandler(world.getBox2d());
//		rays.setAmbientLight(Color.DARK_GRAY);
		
//		new PointLight(rays, 1000, Color.YELLOW.mul(.9f), 50f, 0f, 0f);
		
		for (int i = 0; i < randoms.length; i++) {
			randoms[i] = MathUtils.random();
		}
		
//		blurEffect = new BlurEffect();
//		blurEffect.setDownScale(2f);
//		blurEffect.setBlurFactor(4f);
//		blurEffect.create();
	}
	
	public void render() {
//		blurEffect.begin(renderContext);

		Color color = Color.BLACK;
		Gdx.gl20.glClearColor(color.r, color.g, color.b, color.a);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		player = null;
		world.forAllEntities((e) -> {
			if (e.has(PlayerComponent.class)) {
				camera.position.x += (e.getX() - camera.position.x) * 0.1f;
				camera.position.y += (e.getY() - camera.position.y) * 0.1f;
				
				player = e;
			}
		});
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		
		componentSprites.clear();
		
		world.forAllEntities((e) -> {
			if (e.has(SpritesComponent.class)) {
				for (ComponentSprite cs : e.get(SpritesComponent.class).sprites) {
					Sprite s = cs.sprite;
					
					s.setSize(cs.width, cs.height);
					s.setOrigin(s.getWidth() / 2f, s.getHeight() / 2f);
					s.setCenter(e.getX() + cs.xOffset, e.getY() + cs.yOffset);
					s.setRotation(e.getBody().getAngle() * MathUtils.radiansToDegrees);
					s.setColor(cs.color);
					
					componentSprites.add(cs);
				}
			}
		});
		
		componentSprites.sort((s1, s2) -> Integer.compare(s1.index, s2.index));
		
		//sort sprites
		
		for (ComponentSprite s : componentSprites) {
			s.sprite.draw(batch);
		}
		
//		rays.setCombinedMatrix(camera);
//		rays.updateAndRender();
		
		batch.end();
		
		if(RhomboidGame.DEBUG) {
			if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
				shapeRenderer.setProjectionMatrix(camera.combined);
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.DARK_GRAY);
				for(int i = -100; i <= 100; i++) {
					shapeRenderer.line(i, -100, i, 100);
					shapeRenderer.line(-100, i, 100, i);
				}
				shapeRenderer.end();
			}
			
			debug.render(world.getBox2d(), camera.combined);
		}
		
//		blurEffect.end();
		
		uiCamera.update();
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		
		if(RhomboidGame.DEBUG) {
			Assets.font.draw(batch, String.valueOf(GameState.path), 0f, -uiCamera.zoom + 1f);
		}
		
		batch.end();
	}
	
	public void resize(int width, int height) {
		camera.viewportHeight = 2f;
		camera.viewportWidth = 2f * width / height;
		camera.zoom = 8f;
		
		uiCamera.viewportHeight = 2f;
		uiCamera.viewportWidth = 2f * width / height;
		uiCamera.zoom = 12f;
		
//		blurEffect.resize(width, height);
	}
	
	public OrthographicCamera getCamera() {
		return camera;
	}
}
