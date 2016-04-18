package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;
import walnoot.rhomboid.Time;

public class FadeComponent extends Component {
	private int timer;
	
	@Override
	public void update() {
		timer++;
		
		e.get(SpritesComponent.class).sprites.forEach((s) -> s.color.a = 1f - timer * Time.DELTA);
		
		if(timer == Time.FPS) world.removeEntity(e);
	}
}
