package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;
import walnoot.rhomboid.Entity;
import walnoot.rhomboid.PlayerShape;
import walnoot.rhomboid.RhomboidGame;
import walnoot.rhomboid.Time;
import walnoot.rhomboid.components.SpritesComponent.ComponentSprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

public class PlayerComponent extends Component {
	private static final Vector2 REF = new Vector2(0f, -1f);
	private static final float TORQUE_MULTIPLIER = 200f;
	
	private Array<PlayerShape> shapes;
	private Array<Entity> contacts = new Array<>();
	
	private Fixture current;
	private PlayerShape shape;
	private int index;
	private int timer, lastContactTime;

	private boolean canJump;
	private boolean shouldRespawn;
	
	private Vector2 dustPoint = null;
	
	@Override
	public void init() {
		shapes = world.getShapes();
		setShape(shapes.get(0));
	}
	
	@Override
	public void update() {
		timer++;
		
		if(contacts.size > 0) {
			lastContactTime = timer;
			canJump = true;
		}
		
		int delta = timer - lastContactTime;
		if(delta < 0.25f * Time.FPS) {
			float dir = 0;
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) dir = 1f;
			if (Gdx.input.isKeyPressed(Keys.LEFT)) dir = -1f;

			body.applyTorque(dir * -TORQUE_MULTIPLIER * shape.torque, true);
			body.applyForceToCenter(dir * 20f * shape.boost, 0f, true);

			if (Gdx.input.isKeyPressed(Keys.SPACE) && canJump && shape.jump != 0) {
				canJump = false;
				
				float impulseX = dir * 6f * body.getMass() * shape.jump;
				float impulseY = 12f * body.getMass() * shape.jump;
//				body.applyLinearImpulse(impulseX, impulseY, e.getX(), e.getY(), true);
				body.setLinearVelocity(impulseX, impulseY);
			}
		}
		
		if(e.getY() < -80f) respawn();
		
		float angVel = body.getAngularVelocity();
		
		if(dustPoint != null && Math.abs(body.getAngularVelocity()) > 3f) {
			for(int i = 0; i < 3; i++) {
				Entity dust = world.addEntity("dust");
				dust.setPos(dustPoint);
				dust.getBody().setLinearVelocity(0.5f * body.getAngularVelocity() * MathUtils.random(.25f, 1f), MathUtils.random(.5f, 1.5f));
			}
			
			dustPoint = null;
		}
		
		if(Math.abs(angVel) > 5f * shape.speed) {
			body.setAngularVelocity(5f * shape.speed * Math.signum(angVel));
//			body.applyTorque(-body.getAngularVelocity() * 50 * shape.torque, true);
		}
		
		if(RhomboidGame.DEBUG) {
			if(Gdx.input.isKeyJustPressed(Keys.TAB)) {
				setShape(shapes.get(index++ % shapes.size));
			}
		}
	}
	
	public void respawn() {
//		body.setTransform(0f, 0f, 0f);
//		body.setAngularVelocity(0f);
//		body.setLinearVelocity(0f, 0f);
		
		shouldRespawn = true;
	}

	public void setShape(PlayerShape shape) {
		if(shape != this.shape) {
			FixtureDef def = new FixtureDef();
			def.shape = shape.shape;
			def.friction = 1f;
			def.density = 1f;
			
			if(current != null) body.destroyFixture(current);
			current = body.createFixture(def);
			
			this.shape = shape;
			
			Array<ComponentSprite> sprites = e.get(SpritesComponent.class).sprites;
			sprites.clear();
			if(shape.sprite != null) sprites.add(shape.sprite.getCopy(world.getLoader().getAtlas()));
			
//			for(int i = 0; i < 30; i++) {
//				Entity dust = world.addEntity("change_particle");
//				dust.setPos(body.getPosition());
//				dust.getBody().setLinearVelocity(new Vector2(0f, 1f).rotate(MathUtils.random(360f)).scl(MathUtils.random(4f)));
//			}
		}
	}
	
	public boolean shouldRespawn() {
		return shouldRespawn;
	}
	
	public PlayerShape getShape() {
		return shape;
	}
	
	@Override
	public void beginContact(Contact contact, Entity other) {
		for(int i = 0; i < contact.getWorldManifold().getNumberOfContactPoints(); i++) {
			Vector2 point = contact.getWorldManifold().getPoints()[i];
			
			if(point.y < e.getY()) {
				contacts.add(other);
				
				dustPoint = new Vector2(point);
				
				return;
			}
		}
		
//		float angle = contact.getWorldManifold().getNormal().angle(REF);
//		
//		System.out.println(angle);
//		if(Math.abs(angle) < 70f) {
//			contacts.add(other);
//		}
	}
	
	@Override
	public void endContact(Contact contact, Entity other) {
		contacts.removeValue(other, true);
	}
}
