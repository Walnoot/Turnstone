package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;
import walnoot.rhomboid.Entity;

import com.badlogic.gdx.physics.box2d.Contact;

public class GoalComponent extends Component {
	public String nextLevel;
	private boolean triggered = false;
	
	public GoalComponent(String nextLevel) {
		this.nextLevel = nextLevel;
	}
	
	@Override
	public void beginContact(Contact contact, Entity other) {
		if(other.has(PlayerComponent.class)) triggered = true;
	}
	
	public boolean isTriggered() {
		return triggered;
	}
}
