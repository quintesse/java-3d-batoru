/*
 * Created on 21-mrt-2003
 */
package games.batoru.entities;

import games.batoru.EntityBuilder;

import java.util.Iterator;

import javax.vecmath.*;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.GLUquadric;
import net.java.games.jogl.util.GLUT;

import org.codejive.utils4gl.GLColor;
import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.Vectors;
import org.codejive.world3d.*;

/**
 * @author tako
 */
public class TurretEntity extends Entity implements LiveEntity {
	private int m_nState = STATE_IDLE;
	private float m_fLastHeartbeat = 0;
	private float m_fWaitHeartbeat = 0;
	private float m_fLastFired = 0;
	
	private Vector3f m_barrelDir = new Vector3f();
	
	private Entity m_target = null;
 
	static final private int STATE_IDLE = 1;
	static final private int STATE_TRACKING = 2;
	static final private int STATE_WAITING = 3;
	
	static final private float IDLE_ROTATION_SPEED = (float)((2.0f * Math.PI) / 5.0f); // 5 seconds for a full turn
	
	static final private float TARGET_RADIUS = 45.0f;

	static final private float WAIT_TIME = 2.0f; // 2 seconds
	static final private float FIRE_TIME = 1.0f; // 2 seconds

	// Only here to improve speed
	private Vector3f m_tmpVect = new Vector3f();
	private Matrix3f m_tmpTrans = new Matrix3f();

	private static final GLColor m_baseColor = new GLColor(.5f, .5f, .5f);
	private static final GLColor m_boxColor = new GLColor(0, 0, .9f);
	private static final GLColor m_barrelColor = new GLColor(.5f, 0, 0);
	
	private static final Point3f m_basePos = new Point3f(0.0f, 0.0f, 0.0f);
	private static final Vector3f m_topPos = new Vector3f(0.0f, 1.6f, 0.0f);
	private static final Point3f m_boxPos = new Point3f(0.0f, 0.0f, 0.0f);
	private static final Vector3f m_barrelPos = new Vector3f(1.0f, 0.0f, 0.0f);
	
	public TurretEntity() {
		super();
		m_barrelDir.set(Vectors.VECTF_IN);
	}
	
	public TurretEntity(Universe _universe, Point3f _position) {
		super(_universe, _position, 0.0f);
		m_barrelDir.set(Vectors.VECTF_IN);
	}

	public int getState() {
		return m_nState;
	}

	public Vector3f getBarrelOrientation() {
		return m_barrelDir;
	}
	
	public void heartbeat(float _fTime) {
		Iterator i;
		
		if (m_fLastHeartbeat > 0) {
			float fSecs = _fTime - m_fLastHeartbeat; 
			switch (m_nState) {
				case STATE_IDLE:
					m_tmpTrans.rotY(IDLE_ROTATION_SPEED * fSecs);
					m_tmpTrans.transform(m_barrelDir);
					
					i = m_universe.getLiveEntitiesWithinRadius(getPosition(), TARGET_RADIUS, PlayerEntity.class, true);
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
						m_barrelDir.set(m_tmpVect);

						if ((_fTime - m_fLastFired) >= FIRE_TIME) {
							Universe.log(this, "Shot fired @" + m_target.toString());
							Point3f p = (Point3f)getPosition().clone();
							p.y += 1.6f;
							Entity bullet = EntityBuilder.createBullet(m_universe, p, m_tmpVect, 20.0f, 5.0f);
							m_fLastFired = _fTime;
						}
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
						m_fWaitHeartbeat = _fTime;
						Universe.log(this, "State changed to WAITING");
					}
					break;
				case STATE_WAITING:
					if ((_fTime - m_fWaitHeartbeat) <= WAIT_TIME) {
						i = m_universe.getLiveEntitiesWithinRadius(getPosition(), TARGET_RADIUS, PlayerEntity.class, true);
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
		m_fLastHeartbeat = _fTime;
	}

	public void render(RenderContext _context) {
		GL gl = _context.getGl();
		GLU glu = _context.getGlu();
		GLUT glut = _context.getGlut();

		gl.glPushMatrix();
		gl.glColor3fv(m_baseColor.toArray3f());
		gl.glTranslatef(m_basePos.x, m_basePos.y, m_basePos.z);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glut.glutSolidCone(glu, 0.5f, 1.6f, 12, 1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslatef(m_topPos.x, m_topPos.y, m_topPos.z);
		Vector3f orientation = getBarrelOrientation();
		glu.gluLookAt(0, 0, 0, orientation.x, orientation.y, orientation.z, 0, 1, 0);
		gl.glPushMatrix();
		gl.glColor3fv(m_boxColor.toArray3f());
		gl.glTranslatef(m_boxPos.x, m_boxPos.y, m_boxPos.z);
		gl.glScalef(1.0f, 0.4f, 0.4f);
		glut.glutSolidCube(gl, 1.0f);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glColor3fv(m_barrelColor.toArray3f());
		gl.glTranslatef(m_barrelPos.x, m_barrelPos.y, m_barrelPos.z);
		gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.1d, 0.1d, 0.8d, 6, 1);
		glu.gluDeleteQuadric(quad);
		gl.glPopMatrix();
		gl.glPopMatrix();
	}
}
