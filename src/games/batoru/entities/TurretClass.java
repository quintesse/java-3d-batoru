/*
 * Created on 19-mrt-2003
 */
package games.batoru.entities;

import javax.vecmath.*;

import org.codejive.world3d.*;

/**
 * @author Tako
 */
public class TurretClass extends EntityClass {

	public TurretClass() {
		super("turret", true, true);
	}
		
	public Entity createTurret(Universe _universe, Point3f _position) {
		Entity e = new TurretEntity(_universe, this, (Point3f)_position.clone());
		e.updateState();
		_universe.addLiveEntity((LiveEntity)e);
		return e;
	}
}