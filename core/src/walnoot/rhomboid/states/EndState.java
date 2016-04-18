package walnoot.rhomboid.states;

import walnoot.libgdxutils.State;
import walnoot.rhomboid.Assets;
import walnoot.rhomboid.PrototypeLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EndState extends State {
	private OrthographicCamera camera = new OrthographicCamera();
	private SpriteBatch batch = new SpriteBatch();
	
	private PrototypeLoader loader;
	
	public EndState(PrototypeLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public void render() {
		clearScreen(Color.BLACK);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		
		drawText(0, "The end");
		
		batch.end();

		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			manager.transitionTo(new MenuState(loader), 1f);
		}
	}

	private void drawText(int i, String text) {
		Assets.font.draw(batch, text, 0f, -i * Assets.font.getLineHeight());
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		camera.viewportHeight = 2f;
		camera.viewportWidth = 2f * width / height;
		camera.zoom = 8f;
	}
}
