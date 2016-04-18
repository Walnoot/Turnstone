package walnoot.rhomboid;

import walnoot.rhomboid.components.BodyDefComponent;
import walnoot.rhomboid.components.FadeComponent;
import walnoot.rhomboid.components.FixturesComponent;
import walnoot.rhomboid.components.PlayerComponent;
import walnoot.rhomboid.components.ShapePickupComponent;
import walnoot.rhomboid.components.SpritesComponent;
import walnoot.rhomboid.components.SpritesComponent.ComponentSprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class PrototypeLoader {
	private ObjectMap<String, JsonValue> prototypes = new ObjectMap<>();
	private TextureAtlas atlas;
	private Json json = new Json();
	private JsonValue jsonValue;
	
	public PrototypeLoader(TextureAtlas atlas) {
		this.atlas = atlas;
		
		jsonValue = new JsonReader().parse(Gdx.files.internal("proto.json"));
		
		JsonValue proto = jsonValue.get("protos").child;
		while (proto != null) {
			prototypes.put(proto.name, proto);
			
			proto = proto.next;
		}
		
		json.setSerializer(Shape.class, new ReadOnlySerializer<Shape>() {
			@Override
			@SuppressWarnings("rawtypes")
			public Shape read(Json json, JsonValue jsonData, Class type) {
				if (jsonData.getString(0).equals("box")) {
					PolygonShape box = new PolygonShape();
					Vector2 pos = new Vector2();
					
					if(jsonData.get(4) != null) {
						pos.set(jsonData.getFloat(3), jsonData.getFloat(4));
					}
					
					box.setAsBox(jsonData.getFloat(1), jsonData.getFloat(2), pos, 0f);
					
					return box;
				} else if (jsonData.getString(0).equals("circle")) {
					CircleShape circle = new CircleShape();
					circle.setRadius(jsonData.getFloat(1));
					
					return circle;
				}
				
				return null;
			}
		});
		
		json.setSerializer(ComponentSprite.class, new ReadOnlySerializer<ComponentSprite>() {
			@Override
			@SuppressWarnings("rawtypes")
			public ComponentSprite read(Json json, JsonValue jsonData, Class type) {
				ComponentSprite sprite = new ComponentSprite();
				
				json.readFields(sprite, jsonData);
				sprite.sprite = atlas.createSprite(sprite.name);
				
				return sprite;
			}
			
			@Override
			public void write(Json json, ComponentSprite object, Class knownType) {
				json.writeValue(object, knownType);
			}
		});
		
//		json.setSerializer(Sound.class, new ReadOnlySerializer<Sound>() {
//			@Override
//			@SuppressWarnings("rawtypes")
//			public Sound read(Json json, JsonValue jsonData, Class type) {
//				return Assets.sounds.get(jsonData.asString(), null);
//			}
//		});
	}
	
	public Entity createProto(String name) {
		Entity entity = new Entity();
		
		JsonValue component = prototypes.get(name).child;
		
		ObjectMap<String, Class<? extends Component>> defaultComponents = new ObjectMap<>();
		defaultComponents.put("player", PlayerComponent.class);
		defaultComponents.put("pickup", ShapePickupComponent.class);
		defaultComponents.put("fade", FadeComponent.class);
		
		while (component != null) {
			switch (component.name) {
			case "bodyDef":
				BodyDef def = BodyDefComponent.getDefaultDef();
				json.readFields(def, component);
				
				entity.addComponent(new BodyDefComponent(def));
				
				break;
			case "fixtures":
				FixturesComponent fixtures = new FixturesComponent();
				
				JsonValue fixture = component.child;
				while (fixture != null) {
					FixtureDef fdef = FixturesComponent.getDefaultFixture();
					json.readFields(fdef, fixture);
					
					fixtures.fixtures.add(fdef);
					
					fixture = fixture.next;
				}
				
				entity.addComponent(fixtures);
				break;
			case "sprites":
				SpritesComponent spritesComponent = new SpritesComponent();
				
				JsonValue sprite = component.child;
				while (sprite != null) {
					ComponentSprite cs = json.readValue(ComponentSprite.class, sprite);
//					cs.sprite = atlas.createSprite(cs.name);
					
					spritesComponent.sprites.add(cs);
					
					sprite = sprite.next;
				}
				
				entity.addComponent(spritesComponent);
				break;
			default:
				if(defaultComponents.containsKey(component.name)) {
					Component value = json.readValue(defaultComponents.get(component.name), component);
					entity.addComponent(value);
				} else {
					System.out.println("Unknown component type: " + component.name);
				}
				break;
			}
			
			component = component.next;
		}
		
		return entity;
	}
	
	public TextureAtlas getAtlas() {
		return atlas;
	}
	
	public JsonValue getJsonValue() {
		return jsonValue;
	}
	
	public Json getJson() {
		return json;
	}
}
