package walnoot.rhomboid.desktop;

import walnoot.rhomboid.RhomboidGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class RhomboidLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 1200;
		config.height = 1200 * 9 / 16;
		
		config.samples = 8;
		
		config.title = "Turnstone";
		
		new LwjglApplication(new RhomboidGame(), config);
	}
}
