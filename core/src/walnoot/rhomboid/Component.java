package walnoot.rhomboid;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;

public abstract class Component {
	protected Entity e;
	protected GameWorld world;
	public Body body;
	
	public void update() {
	}
	
	public void addTo(Entity e) {
		if(this.e != null) {
			throw new IllegalStateException("Component already bound to Entity");
		} else {
			this.e = e;
		}
	}
	
	public void init() {
	}
	
	public void beginContact(Contact contact, Entity other) {
	}
	
	public void endContact(Contact contact, Entity other) {
	}

	public void setWorld(GameWorld world, Body body) {
		this.world = world;
		this.body = body;
	}

	public void onRemove() {
	}

	public void newComponent(Component c) {
	}
}
