/*
 * Created on Nov 8, 2003
 */
package games.batoru;

import games.batoru.entities.BulletEntity;
import games.batoru.entities.PlayerEntity;
import games.batoru.entities.TreeEntity;
import games.batoru.entities.TurretEntity;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.utils4gl.Vectors;
import org.codejive.world3d.Entity;
import org.codejive.world3d.LiveEntity;
import org.codejive.world3d.Universe;

/**
 * @author tako
 */
public class EntityBuilder {
	
	public static Entity createBullet(Universe _universe, Point3f _position, Vector3f _orientation, float _fSpeed, float _fLifetime) {
		Entity e = new BulletEntity(_universe, new Point3f(_position), Vectors.getScaledVector(_orientation, _fSpeed), 0.0f);
		e.setLifetime(_fLifetime);
		e.updateState();
		if (e instanceof LiveEntity) {
			_universe.addLiveEntity((LiveEntity)e);
		}
		return e;
	}
	
	public static Entity createTree(Universe _universe, Point3f _position) {
		Entity e = new TreeEntity(_universe, (Point3f)_position.clone());
		e.updateState();
		return e;
	}
		
	public static Entity createTurret(Universe _universe, Point3f _position) {
		Entity e = new TurretEntity(_universe, (Point3f)_position.clone());
		e.updateState();
		_universe.addLiveEntity((LiveEntity)e);
		return e;
	}

	public static Entity createPlayer(Universe _universe, Point3f _position, Vector3f _orientation, float _fEyeHeight) {
		Entity e = new PlayerEntity(_universe, _position, _orientation, _fEyeHeight);
		e.updateState();
		if (e instanceof LiveEntity) {
			_universe.addLiveEntity((LiveEntity)e);
		}
		return e;
	}
}
