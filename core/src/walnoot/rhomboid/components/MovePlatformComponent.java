package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;
import walnoot.rhomboid.GameWorld;
import walnoot.rhomboid.Time;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class MovePlatformComponent extends Component {
	public float speed = 4f;
	public float moveX, moveY;
	public float startX, startY;
	
	private int timer;
	
	@Override
	public void setWorld(GameWorld world, Body body) {
		super.setWorld(world, body);
		
		if(body != null) {
			body.setType(BodyType.KinematicBody);
		}
	}
	
	@Override
	public void update() {
		timer++;
		
		int idleTicks = (int) Time.FPS;
		int moveTicks = (int) (Time.FPS * (moveX + moveY));
		
		if(timer >= 2 * (idleTicks + moveTicks)) timer = 0;
		
		if(timer < idleTicks) {
			body.setLinearVelocity(0f, 0f);
		} else if(timer < idleTicks + moveTicks) {
			body.setLinearVelocity(Math.signum(moveX) * speed, Math.signum(moveY) * speed);
		} else if(timer < 2 * idleTicks + moveTicks) {
			body.setLinearVelocity(0f, 0f);
		} else {
			body.setLinearVelocity(-Math.signum(moveX) * speed, -Math.signum(moveY) * speed);
		}
	}
}
