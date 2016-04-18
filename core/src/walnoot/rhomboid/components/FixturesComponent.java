package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

public class FixturesComponent extends Component {
	public final Array<FixtureDef> fixtures = new Array<>();
	
	public void addCircle(float radius) {
		FixtureDef fixtureDef = getDefaultFixture();
		
		CircleShape circle = new CircleShape();
		circle.setRadius(radius);
		fixtureDef.shape = circle;
		
		fixtures.add(fixtureDef);
	}
	
	public void addBox(float width, float height) {
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);
		
		FixtureDef fixtureDef = getDefaultFixture();
		fixtureDef.shape = shape;
		
		fixtures.add(fixtureDef);
	}
	
	public static FixtureDef getDefaultFixture() {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 1f;
		fixtureDef.density = 1f;
		fixtureDef.restitution = 0.1f;
		
		return fixtureDef;
	}
}
