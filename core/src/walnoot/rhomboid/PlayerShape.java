package walnoot.rhomboid;

import walnoot.rhomboid.components.SpritesComponent.ComponentSprite;

import com.badlogic.gdx.physics.box2d.Shape;

public class PlayerShape {
	public Shape shape;
	public float torque = 1f, speed = 1f, boost = 0.5f, jump = 0f;
	public String name;
	public ComponentSprite sprite;
}
