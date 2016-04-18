package walnoot.rhomboid;

import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public final class Entity {
	private Array<Component> components = new Array<>();
	private IntMap<Component> map = new IntMap<>();
	private GameWorld world;
	private Body body;
	public String name = "?";
	
	public void update() {
		forAllComponents((c) -> c.update());
	}
	
	public void addComponent(Component c) {
		if(get(c.getClass()) != null) {
			throw new IllegalStateException("This entity already contains a component of type " + c.getClass());
		} else {
			//notify other components that a new component was added
			//and let the new component know which components there are
			for (Component component : components) {
				component.newComponent(c);
				c.newComponent(component);
			}
			
			components.add(c);
			c.addTo(this);
			c.setWorld(world, body);
			map.put(c.getClass().hashCode(), c);
		}
	}
	
	public void removeComponent(Component c) {
		if(components.removeValue(c, true)) {
			c.setWorld(null, null);
			c.e = null;
			map.remove(c.getClass().hashCode());
		} else {
			throw new IllegalStateException("This entity does not contain a component of type " + c.getClass());
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> T get(Class<T> clazz) {
		return (T) map.get(clazz.hashCode(), null);
	}
	
	public boolean has(Class<? extends Component> clazz) {
		return get(clazz) != null;
	}
	
	public void forAllComponents(Consumer<Component> c) {
		for(int i = 0; i < components.size; i++) {
			c.accept(components.get(i));
		}
	}
	
	public void setWorld(GameWorld world, Body body) {
		this.world = world;
		this.body = body;
		
		forAllComponents((c) -> c.setWorld(world, body));
	}
	
	public void beginContact(Contact contact, Entity other) {
		forAllComponents((c) -> c.beginContact(contact, other));
	}
	
	public void endContact(Contact contact, Entity other) {
		forAllComponents((c) -> c.endContact(contact, other));
	}
	
	public void onRemove() {
		forAllComponents((c) -> c.onRemove());
	}
	
	public float getX() {
		return body.getPosition().x;
	}
	
	public float getY() {
		return body.getPosition().y;
	}
	
	public void setPos(Vector2 pos) {
		body.setTransform(pos, body.getAngle());
	}
	
	public Body getBody() {
		return body;
	}
}
