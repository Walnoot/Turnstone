package walnoot.rhomboid.states;

import walnoot.libgdxutils.State;
import walnoot.rhomboid.Assets;
import walnoot.rhomboid.PrototypeLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MenuState extends State {
	private OrthographicCamera camera = new OrthographicCamera();
	private SpriteBatch batch = new SpriteBatch();
	
	private int index;
	private PrototypeLoader loader;
	private GameState previous;
	
	public MenuState(PrototypeLoader loader) {
		this(loader, null);
	}
	
	public MenuState(PrototypeLoader loader, GameState previous) {
		this.loader = loader;
		this.previous = previous;
	}
	
	@Override
	public void render() {
		clearScreen(Color.BLACK);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		
		drawText(-2, "[GREEN]Turnstone");
		
		for(int i = 0; i < MenuItem.values().length; i++) {
			MenuItem item = MenuItem.values()[i];
			
			String color = i == index ? "[WHITE]" : "[GRAY]";
			
			String text = item.text;
			if(item == MenuItem.PLAY && previous != null) text = "Resume Game";
			
			drawText(i, color + text);
		}
		
		batch.end();

		if(Gdx.input.isKeyJustPressed(Keys.DOWN)) index++;
		if(Gdx.input.isKeyJustPressed(Keys.UP)) index--;
		
		int max = MenuItem.values().length;
		if(index < 0) index += max;
		if(index >= max) index -= max;
		
		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			switch (MenuItem.values()[index]) {
			case PLAY:
				if(previous == null) {
					manager.transitionTo(new GameState(loader, "l0"), 1f);
				} else {
					manager.transitionTo(previous, 1f);
				}
				break;
			case FULLSCREEN:
				if(Gdx.graphics.isFullscreen()) {
					Gdx.graphics.setWindowedMode(1200, 1200 * 9 / 16);
				} else {
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				}
				
				break;
			case EXIT:
				Gdx.app.exit();
				
				break;
			default:
				break;
			}
		}
	}

	private void drawText(int i, String text) {
		Assets.font.draw(batch, text, 0f, -i * Assets.font.getLineHeight());
	}
	
	@Override
	public void update() {
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE) && previous != null) {
			manager.transitionTo(previous, .3f);
		}
	}

	@Override
	public void resize(boolean creation, int width, int height) {
		camera.viewportHeight = 2f;
		camera.viewportWidth = 2f * width / height;
		camera.zoom = 8f;
	}
	
	private enum MenuItem {
		PLAY("Play Game"), FULLSCREEN("Toggle Fullscreen"), EXIT("Exit");
		
		private String text;

		private MenuItem(String text) {
			this.text = text;
		}
	}
}
