package walnoot.rhomboid.components;

import walnoot.rhomboid.Component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class SpritesComponent extends Component {
	public final Array<ComponentSprite> sprites = new Array<ComponentSprite>();
	
	public static class ComponentSprite {
		public transient Sprite sprite;
		public String name;
		public int index;
		public float width, height;
		public float xOffset, yOffset;
		public final Color color = new Color(Color.WHITE);
		
		public ComponentSprite getCopy(TextureAtlas atlas) {
			ComponentSprite copy = new ComponentSprite();
			
			copy.name = name;
			copy.index = index;
			copy.width = width;
			copy.height = height;
			copy.xOffset = xOffset;
			copy.yOffset = yOffset;
			copy.color.set(color);
			copy.sprite = atlas.createSprite(name);
			
			return copy;
		}
	}
}
