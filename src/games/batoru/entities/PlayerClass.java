/*
 * Created on 24-mrt-2003
 */
package games.batoru.entities;

import javax.vecmath.*;

import org.codejive.world3d.*;

/**
 * @author tako
 */
public class PlayerClass extends EntityClass {
	public PlayerClass() {
		super("player", true, true);
	}
		
	public Entity createPlayer(Universe _universe, Point3f _position, Vector3f _orientation, float _fEyeHeight) {
		Entity e = new PlayerEntity(_universe, this, _position, _orientation, _fEyeHeight);
		e.updateState();
		if (e instanceof LiveEntity) {
			_universe.addLiveEntity((LiveEntity)e);
		}
		return e;
	}
}
