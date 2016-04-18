package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;
import walnoot.rhomboid.Entity;
import walnoot.rhomboid.PlayerShape;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class ShapePickupComponent extends Component {
	public String shape;
	private PlayerComponent player;
	
	@Override
	public void init() {
		PlayerShape playerShape = world.getShape(shape);
		
		FixtureDef def = new FixtureDef();
		def.shape = playerShape.shape;
		def.isSensor = true;
		
		body.createFixture(def);
		body.setAngularVelocity(1f);
		
		e.get(SpritesComponent.class).sprites.add(playerShape.sprite);
	}
	
	@Override
	public void update() {
		if(player != null) {
			PlayerShape newShape = world.getShape(shape);
			
			if(newShape != player.getShape()) {
				for(int i = 0; i < 30; i++) {
					Entity dust = world.addEntity("change_particle");
					dust.setPos(player.body.getPosition());
					dust.getBody().setLinearVelocity(new Vector2(0f, 1f).rotate(MathUtils.random(360f)).scl(MathUtils.random(4f)));
				}
			}
			
			player.setShape(newShape);
			player = null;
		}
	}
	
	@Override
	public void beginContact(Contact contact, Entity other) {
		PlayerComponent playerComponent = other.get(PlayerComponent.class);
		if(playerComponent != null) player = playerComponent;
	}
}
