/*
 * Created on 18-mrt-2003
 */
package games.batoru.entities;

import javax.vecmath.*;

import org.codejive.world3d.*;

/**
 * @author Tako
 */
public class TreeClass extends EntityClass {

	public TreeClass() {
		super("tree", false, true);
	}
	
	public Entity createEntity(Universe _universe, Point3f _position) {
		Entity e = new Entity(_universe, this, (Point3f)_position.clone(), 0.0f);
		e.updateState();
		return e;
	}
}
