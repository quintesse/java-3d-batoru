/*
 * Created on Nov 8, 2003
 */
package games.batoru;

import games.batoru.entities.BulletClass;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.world3d.Entity;
import org.codejive.world3d.Shape;
import org.codejive.world3d.Universe;

/**
 * @author tako
 */
public class EntityBuilder {
	private static BulletClass m_bulletClass = new BulletClass();
	
	public static Entity createBullet(Universe _universe, Point3f _position, Vector3f _direction, float _fSpeed, float _fLifetime) {
		return m_bulletClass.createEntity(_universe, _position, _direction, _fSpeed, _fLifetime);
	}
	
	public static Shape createBulletShape(Universe _universe, Point3f _position, Vector3f _direction, float _fSpeed, float _fLifetime) {
		return m_bulletClass.createShape(_universe, _position, _direction, _fSpeed, _fLifetime);
	}
}
