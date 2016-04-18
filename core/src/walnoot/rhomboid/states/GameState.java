package walnoot.rhomboid.states;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import walnoot.libgdxutils.State;
import walnoot.rhomboid.Entity;
import walnoot.rhomboid.GameWorld;
import walnoot.rhomboid.PrototypeLoader;
import walnoot.rhomboid.RhomboidGame;
import walnoot.rhomboid.WorldRenderer;
import walnoot.rhomboid.components.FixturesComponent;
import walnoot.rhomboid.components.GoalComponent;
import walnoot.rhomboid.components.MovePlatformComponent;
import walnoot.rhomboid.components.PlayerComponent;
import walnoot.rhomboid.components.ShapePickupComponent;
import walnoot.rhomboid.components.SpritesComponent;
import walnoot.rhomboid.components.SpritesComponent.ComponentSprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class GameState extends State {
	private final GameWorld world;
	private WorldRenderer renderer;
	private EditorInput editor = new EditorInput();
	
	public static String path;
	private PrototypeLoader loader;
	private String level = "l0";
	
	public GameState(PrototypeLoader loader) {
		this.loader = loader;
		world = new GameWorld(loader);

		world.addEntity("player");
		world.addEntity("floor").getBody().setTransform(0f, -3f, 0f);
	}
	
	public GameState(PrototypeLoader loader, String level) {
		this(loader);
		this.level = level;
		
		loadLevel(level);
		path = level;
	}
	
	@Override
	public void show() {
		if(RhomboidGame.DEBUG) RhomboidGame.inputs.addProcessor(editor);
		
		if(renderer == null) {
			renderer = new WorldRenderer(world, manager.getRenderContext());
		}
	}
	
	@Override
	public void hide() {
		if(RhomboidGame.DEBUG) RhomboidGame.inputs.removeProcessor(editor);
	}
	
	@Override
	public void update() {
		world.update();
		
		world.stream()
			.map((e) -> e.get(GoalComponent.class))
			.filter((g) -> g != null)
			.filter((g) -> g.isTriggered())
			.findAny()
			.ifPresent((g) -> {
				if(g.nextLevel.equals("end")) {
					manager.transitionTo(new EndState(loader), 1f);
				} else {
					manager.transitionTo(new GameState(loader, g.nextLevel), 1f);
				}
			});
		
		world.stream()
			.map((e) -> e.get(PlayerComponent.class))
			.filter((g) -> g != null)
			.filter((g) -> g.shouldRespawn())
			.findAny()
			.ifPresent((p) -> manager.transitionTo(new GameState(loader, level), .3f));
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)) manager.transitionTo(new MenuState(loader, this), .3f);
	}
	
	@Override
	public void render() {
		clearScreen(Color.BLACK);
		
		renderer.render();
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		renderer.resize(width, height);
	}

	private void loadLevel(String path) {
		try {
			Json json = loader.getJson();
//			JsonValue jsonValue = new JsonReader().parse(new FileInputStream(path + ".json"));
			JsonValue jsonValue = new JsonReader().parse(Gdx.files.internal(path + ".json"));
			
			JsonValue object = jsonValue.get("objects").child;
			while(object != null) {
				JsonValue shapeJson = object.get("shape");
				Vector2[] vertices = new Vector2[shapeJson.size];
				
				int i = 0;
				JsonValue vector = shapeJson.child;
				while(vector != null) {
					vertices[i++] = json.readValue(Vector2.class, vector);
					
					vector = vector.next;
				}
				
				Entity e = world.addEntity("object");
				
				PolygonShape shape = new PolygonShape();
				shape.set(vertices);
				
				FixtureDef def = FixturesComponent.getDefaultFixture();
				def.shape = shape;
				
				if(object.has("goal")) {
					e.addComponent(new GoalComponent(object.getString("goal")));
					def.isSensor = true;
				}
				
				if(object.has("pos")) {
					e.setPos(json.readValue("pos", Vector2.class, object));
				}
				
				if(object.has("movePlatform")) {
					MovePlatformComponent move = json.readValue("movePlatform", MovePlatformComponent.class, object);
					e.addComponent(move);
					
					e.setPos(new Vector2(move.startX, move.startY));
				}
				
				if(object.has("sprites")) {
					SpritesComponent sComp = new SpritesComponent();
					for(JsonValue sprite : object.get("sprites")) {
						ComponentSprite cs = json.readValue(ComponentSprite.class, sprite);
//						cs.sprite = loader.getAtlas().createSprite(cs.name);
						
						sComp.sprites.add(cs);
					}
					
					e.addComponent(sComp);
				}
				
				e.getBody().createFixture(def);
				
				object = object.next;
			}
			
			for(JsonValue pickup : jsonValue.get("pickups")) {
				Entity e = world.getLoader().createProto("pickup");
				String shape;
				
				do {
					shape = pickup.getString("shape");
				} while(world.getShape(shape) == null);
				
				e.get(ShapePickupComponent.class).shape = shape;
				world.addEntity(e);
				
				e.setPos(json.readValue(Vector2.class, pickup.get("pos")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class EditorInput extends InputAdapter {
		private Entity selection;
		
		private Vector3 pos = new Vector3();
		private Array<Vector2> positions = new Array<Vector2>();
		
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			renderer.getCamera().unproject(pos.set(screenX, screenY, 0f));
			
			Vector2 point = new Vector2(pos.x, pos.y);
			Vector2 roundPoint = new Vector2(MathUtils.round(pos.x), MathUtils.round(pos.y));			
			
			if (button == Buttons.LEFT) {
				if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
					System.out.println("adding point");
					
					positions.add(roundPoint);
				} else {
					selection = null;
					world.queryPoint(point, (e) -> {
						System.out.println("Selected");
						
						selection = e;
						positions.clear();
					});
				}
			} else if(button == Buttons.RIGHT) {
				Entity pickup = world.getLoader().createProto("pickup");
				String shape;
				
				do {
					shape = JOptionPane.showInputDialog("shape");
				} while(world.getShape(shape) == null);
				
				pickup.get(ShapePickupComponent.class).shape = shape;
				world.addEntity(pickup);
				
				pickup.setPos(roundPoint);
			}
			
			return true;
		}
		
		@Override
		public boolean keyDown(int keycode) {
			boolean handled = true;
			
			switch (keycode) {
			case Keys.ENTER:
				if (positions.size > 2) {
					Entity object = world.addEntity("object");
					
					Vector2 origin = new Vector2();
					for(Vector2 pos : positions) {
						origin.x += pos.x;
						origin.y += pos.y;
					}
					origin.scl(1f / positions.size);
					
					PolygonShape shape = new PolygonShape();
					float[] vertices = new float[2 * positions.size];
					for (int i = 0; i < positions.size; i++) {
						vertices[2 * i] = positions.get(i).x - origin.x;
						vertices[2 * i + 1] = positions.get(i).y - origin.y;
					}
					shape.set(vertices, 0, vertices.length);
					
					object.getBody().createFixture(shape, 1f);
					
					object.setPos(origin);
					
					positions.clear();
				}
				break;
			case Keys.BACKSPACE:
				positions.clear();
				
				if(selection != null) world.removeEntity(selection);
				
				break;
			case Keys.G:
				if(selection != null && !selection.has(GoalComponent.class)) {
					selection.addComponent(new GoalComponent(JOptionPane.showInputDialog("next level:")));
				}
				
				break;
			case Keys.S:
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
					try {
						if(path == null || Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
							path = JOptionPane.showInputDialog("Save as..");
						}
						
						Json json = new Json();
						File file = new File(path + ".json");
						file.createNewFile();
						PrintWriter writer = new PrintWriter(file);
						json.setWriter(writer);
						
						json.writeObjectStart();
						
						json.writeArrayStart("objects");
						
						Vector2 vertex = new Vector2();
						
						world.stream().filter((e) -> e.name.equals("object")).forEach((e) -> {
							json.writeObjectStart();
							
							Array<Vector2> shapeArray = new Array<>();
							
							PolygonShape shape = (PolygonShape) e.getBody().getFixtureList().get(0).getShape();
							for (int i = 0; i < shape.getVertexCount(); i++) {
								shape.getVertex(i, vertex);
								shapeArray.add(new Vector2(vertex));
							}
							json.writeValue("shape", shapeArray);
							
							json.writeValue("pos", e.getBody().getPosition());
							
							if(e.has(GoalComponent.class)) {
								json.writeValue("goal", e.get(GoalComponent.class).nextLevel);
							}
							
							if(e.has(MovePlatformComponent.class)) {
								json.writeObjectStart("movePlatform");
								MovePlatformComponent component = e.get(MovePlatformComponent.class);
								json.writeField(component, "moveX");
								json.writeField(component, "moveY");
								json.writeField(component, "startX");
								json.writeField(component, "startY");
								json.writeObjectEnd();
							}
							
							if(e.has(SpritesComponent.class)) {
								json.writeArrayStart("sprites");
								
								for(ComponentSprite cs : e.get(SpritesComponent.class).sprites) {
									json.writeObjectStart();
									json.writeField(cs, "name");
									json.writeField(cs, "index");
									json.writeField(cs, "width");
									json.writeField(cs, "height");
									json.writeField(cs, "xOffset");
									json.writeField(cs, "yOffset");
									json.writeObjectEnd();
								}
								
								json.writeArrayEnd();
							}
							
							json.writeObjectEnd();
						});

						json.writeArrayEnd();
						
						json.writeArrayStart("pickups");
						
						world.stream().filter((e) -> e.has(ShapePickupComponent.class)).forEach((e) -> {
							json.writeObjectStart();
							json.writeValue("pos", e.getBody().getPosition());
							json.writeValue("shape", e.get(ShapePickupComponent.class).shape);
							json.writeObjectEnd();
						});

						json.writeArrayEnd();
						json.writeObjectEnd();
						
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				break;
			case Keys.O:
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
					path = JOptionPane.showInputDialog("Open..", GameState.path);
					
					manager.setState(new GameState(loader, path));
				}
				
				break;
			case Keys.N:
				if(selection != null) {
					JFileChooser chooser = new JFileChooser(new File("../core/assets"));
					chooser.setFileFilter(new FileNameExtensionFilter(null, "png"));
					if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						String name = chooser.getSelectedFile().getName().replaceAll("\\..*", "");
						
						Sprite sprite = loader.getAtlas().createSprite(name);
						ComponentSprite cs = new ComponentSprite();
						cs.sprite = sprite;
						cs.width = sprite.getWidth() / 64;
						cs.height = sprite.getHeight() / 64;
						cs.name = name;
						
						if(!selection.has(SpritesComponent.class)) {
							selection.addComponent(new SpritesComponent());
						}
						
						selection.get(SpritesComponent.class).sprites.add(cs);
					}
				}
				
				break;
			case Keys.M:
				if(selection != null) {
					if(!selection.has(MovePlatformComponent.class)) selection.addComponent(new MovePlatformComponent());

					float x = Float.parseFloat(JOptionPane.showInputDialog("x speed", 0));
					float y = Float.parseFloat(JOptionPane.showInputDialog("y speed", 0));

					MovePlatformComponent move = selection.get(MovePlatformComponent.class);
					move.moveX = x;
					move.moveY = y;

					move.startX = selection.getX();
					move.startY = selection.getY();
				}
				
				break;
			case Keys.NUMPAD_4:
			case Keys.NUMPAD_6:
			case Keys.NUMPAD_8:
			case Keys.NUMPAD_2:
				int[] keys = {Keys.NUMPAD_4, Keys.NUMPAD_6, Keys.NUMPAD_8, Keys.NUMPAD_2};
				int[] xs = {-1, 1, 0, 0};
				int[] ys = {0, 0, 1, -1};
				
				int xtemp = 0, ytemp = 0;
				for(int i = 0; i < 4; i++) {
					if(keycode == keys[i]) {
						xtemp = xs[i];
						ytemp = ys[i];
					}
				}
				
				final int x = xtemp, y = ytemp;
				
				if (selection != null && selection.has(SpritesComponent.class)) {
					ComponentSprite sprite = selection.get(SpritesComponent.class).sprites.peek();
					if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
						sprite.width += x;
						sprite.height += y;
					} else {
						sprite.xOffset += x * 0.5f;
						sprite.yOffset += y * 0.5f;
					}
				}
				break;
			case Keys.NUMPAD_5:
				if (selection != null && selection.has(SpritesComponent.class)) {
					Array<ComponentSprite> sprites = selection.get(SpritesComponent.class).sprites;
					
					ComponentSprite sprite = sprites.removeIndex(0);
					sprites.add(sprite);
				}
				break;
			case Keys.NUMPAD_0:
				if (selection != null && selection.has(SpritesComponent.class)) {
					selection.get(SpritesComponent.class).sprites.pop();
				}
				break;
			default:
				handled = false;
				break;
			}
			
			return handled;
		}
	}
}
