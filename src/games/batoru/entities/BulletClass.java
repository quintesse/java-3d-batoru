/*
 * Created on 19-mrt-2003
 */
package games.batoru.entities;

import games.batoru.shapes.BulletShape;

import javax.vecmath.*;

import org.codejive.world3d.*;
import org.codejive.utils4gl.Vectors;

/**
 * @author Tako
 */
public class BulletClass extends EntityClass {
	 
	public BulletClass() {
		super("bullet", false, false);
	}

	public Entity createEntity(Universe _universe, Point3f _position, Vector3f _direction, float _fSpeed, float _fLifetime) {
		Entity e = new Entity(_universe, this, new Point3f(_position), Vectors.getScaledVector(_direction, _fSpeed), 0.0f);
		e.setLifetime(_fLifetime);
		e.updateState();
		if (e instanceof LiveEntity) {
			_universe.addLiveEntity((LiveEntity)e);
		}
		return e;
	}

	public Shape createShape(Universe _universe, Point3f _position, Vector3f _direction, float _fSpeed, float _fLifetime) {
		Shape e = new BulletShape();
		e.setUniverse(_universe);
		e.setPosition(_position);
		e.setImpulse(Vectors.getScaledVector(_direction, _fSpeed));
		e.setLifetime(_fLifetime);
		e.setGravityFactor(0.0f);
		e.updateState();
		if (e instanceof LiveEntity) {
			_universe.addLiveEntity((LiveEntity)e);
		}
		return e;
	}
}