package walnoot.rhomboid;

import java.util.Scanner;

import walnoot.libgdxutils.State;
import walnoot.libgdxutils.StateApplication;
import walnoot.rhomboid.states.MenuState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class RhomboidGame extends StateApplication {
	public static final boolean DEBUG = false;

	public static final InputMultiplexer inputs = new InputMultiplexer();
	
	private PrototypeLoader loader;

	public RhomboidGame() {
		super(Time.FPS, DEBUG);
	}
	
	@Override
	protected void update() {
		super.update();
		
		if(Gdx.input.isKeyJustPressed(Keys.F5)) {
			setState(getFirstState());
		}
	}
	
	@Override
	protected void init() {
		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 2, false);
		
//		for(FileHandle file : Gdx.files.local("../core/assets").list(".png")) {
//			packer.pack(file.nameWithoutExtension(), new Pixmap(file));
//		}
		
		Scanner scanner = new Scanner(Gdx.files.internal("filelist").read());
		
		while(scanner.hasNextLine()) {
			String file = scanner.nextLine();
			packer.pack(file.replaceAll("\\..*", ""), new Pixmap(Gdx.files.internal(file)));
		}
		
		scanner.close();
		
		TextureAtlas atlas = packer.generateTextureAtlas(TextureFilter.MipMapLinearLinear, TextureFilter.Linear, true);
		loader = new PrototypeLoader(atlas);
		
		Gdx.input.setInputProcessor(inputs);
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("KaushanScript-Regular.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 48;
		parameter.packer = packer;
		parameter.magFilter = TextureFilter.Linear;
		parameter.minFilter = TextureFilter.MipMapLinearLinear;
		parameter.genMipMaps = true;
		
		Assets.font = generator.generateFont(parameter);
		Assets.font.setUseIntegerPositions(false);
		Assets.font.getData().setScale(1f / parameter.size);
		Assets.font.getData().markupEnabled = true;
	}

	@Override
	protected State getFirstState() {
		return new MenuState(loader);
//		return new GameState(loader);
	}
	
	@Override
	protected int getExitKey() {
		return Keys.GRAVE;
	}
}
