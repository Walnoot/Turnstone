package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class BodyDefComponent extends Component {
	public final BodyDef def;
	
	public BodyDefComponent() {
		this(getDefaultDef());
	}
	
	public BodyDefComponent(BodyDef def) {
		this.def = def;
	}
	
	public static BodyDef getDefaultDef() {
		BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.linearDamping = 0.05f;
		
		return def;
	}
}
