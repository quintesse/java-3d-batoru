/*
 * Created on 21-mrt-2003
 */
package games.batoru.entities;

import java.util.Iterator;

import javax.vecmath.*;

import org.codejive.world3d.*;

/**
 * @author tako
 */
public class TurretEntity extends Entity implements LiveEntity {
	private int m_nState = STATE_IDLE;
	private long m_nLastHeartbeat = 0;
	private long m_nWaitHeartbeat = 0;
	private long m_nLastFired = 0;
	
	private Matrix3f m_barrelTrans = new Matrix3f();
	
	private Entity m_target = null;
 
	static final private int STATE_IDLE = 1;
	static final private int STATE_TRACKING = 2;
	static final private int STATE_WAITING = 3;
	
	static final private float IDLE_ROTATION_SPEED = (float)((2.0f * Math.PI) / 5.0f); // 5 seconds for a full turn
	
	static final private float TARGET_RADIUS = 45.0f;

	static final private int WAIT_TIME = 2000; // 2 seconds
	static final private int FIRE_TIME = 1000; // 2 seconds

	// Only here to improve speed
	private Vector3f m_tmpVect = new Vector3f();
	private Matrix3f m_tmpTrans = new Matrix3f();

	public TurretEntity(Universe _universe, EntityClass _class, Point3f _position) {
		super(_universe, _class, _position, 0.0f);
		m_barrelTrans.setIdentity();
	}

	public int getState() {
		return m_nState;
	}

	public Matrix3f getBarrelOrientation() {
		return m_barrelTrans;
	}
	
	public void heartbeat(long _time) {
		Iterator i;
		
		if (m_nLastHeartbeat > 0) {
			float fSecs = (float)(_time - m_nLastHeartbeat) / 1000; 
			switch (m_nState) {
				case STATE_IDLE:
					m_tmpTrans.rotY(IDLE_ROTATION_SPEED * fSecs);
					m_barrelTrans.mul(m_tmpTrans);
					
					i = m_universe.getLiveEntitiesWithinRadius(getPosition(), TARGET_RADIUS, PlayerClass.class, true);
					if (i.hasNext()) {
						m_nState = STATE_TRACKING;
						m_target = (Entity)i.next();
						Universe.log(this, "State changed to TRACKING " + m_target.toString());
					}
					break;
				case STATE_TRACKING:
					if (getPosition().distanceSquared(m_target.getPosition()) <= (TARGET_RADIUS * TARGET_RADIUS)) {
						m_tmpVect.set(m_target.getPosition());
						m_tmpVect.sub(getPosition());
						m_tmpVect.normalize();
/*
						Point3d q = new Point3d(getPosition());
						Vector3d v = new Vector3d(m_tmpVect);
						PickRay ray = new PickRay(q, v);
						m_transShape.setPickable(false); // Make sure we don't pick ourself
						PickTool pt = new PickTool(m_universe.getScene());
						pt.setMode(PickTool.GEOMETRY);
						pt.setShapeRay(q, v);
						PickResult pr = pt.pickClosest();
						m_transShape.setPickable(isSolid());
//						Universe.log("### turret sees: ");
						if ((pr != null) && pr.getSceneGraphPath().nodeCount() > 0) {
							Entity pick = (Entity)pr.getSceneGraphPath().getNode(0).getUserData();
							PickIntersection pi = pr.getClosestIntersection(q);
//							Universe.log("    " + pr.getObject() + " - " + pick + " dist: " + pi.getDistance());
							if ((pick != null) && (pick instanceof Player)) {
								Point3d c = new Point3d(0.0d, 0.0d, 0.0d);
								Point3d t = new Point3d(m_tmpVect);
								t.x = -t.x;
								t.y = 0;
								Vector3d up = Vectors.VECTD_UP;
								m_barrelTrans.lookAt(c, t, up);
//		  Universe.log("turret: " + getPosition() + " tracks: " + m_target.getPosition() + " (" + m_barrelDirection + ")");
	
								if ((_time - m_nLastFired) >= FIRE_TIME) {
									Universe.log(this, "Shot fired @" + m_target.toString());
									Point3f p = (Point3f)getPosition().clone();
									p.y += 1.6f;
									Entity bullet = m_universe.createBullet(p, m_tmpVect, 20.0f, 5.0f);
									m_universe.addEntity(bullet);
									m_nLastFired = _time;
	
									ComplexShape3D turret = (ComplexShape3D)getShape();
									PointSound sound = (PointSound)turret.getActivePart(TurretClass.PART_SOUND);
									sound.setEnable(true);
								}
							}
						} else {
//							Universe.log("turret sees: nothing!");
						}
*/
					} else {
						m_nState = STATE_WAITING;
						m_target = null;
						m_nWaitHeartbeat = _time;
						Universe.log(this, "State changed to WAITING");
					}
					break;
				case STATE_WAITING:
					if ((_time - m_nWaitHeartbeat) <= WAIT_TIME) {
						i = m_universe.getLiveEntitiesWithinRadius(getPosition(), TARGET_RADIUS, PlayerClass.class, true);
						if (i.hasNext()) {
							m_nState = STATE_TRACKING;
							m_target = (Entity)i.next();
							Universe.log(this, "State changed to TRACKING " + m_target.toString());
						}
					} else {
						m_nState = STATE_IDLE;
						Universe.log(this, "State changed to IDLE");
					}
					break;
			}
		}
		m_nLastHeartbeat = _time;
	}

	/* (non-Javadoc)
	 * @see test.Entity#terminateEntity()
	 */
	public void terminateEntity() {
		m_universe.removeLiveEntity(this);
	}
}
