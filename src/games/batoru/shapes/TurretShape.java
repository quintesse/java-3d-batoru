/*
 * Created on 19-mrt-2003
 */
package games.batoru.shapes;

import javax.vecmath.*;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.GLUquadric;
import net.java.games.jogl.util.GLUT;

import org.codejive.world3d.*;
import org.codejive.utils4gl.*;

import games.batoru.entities.Turret;

/**
 * @author Tako
 */
public class TurretShape extends Shape implements Renderable, Turret {
	private Vector3f m_barrelOrientation = new Vector3f();

	public static final int NO_PARTS = 2;
	public static final int PART_TOP = 0;
	public static final int PART_SOUND = 1;
	
	private static final GLColor m_baseColor = new GLColor(.5f, .5f, .5f);
	private static final GLColor m_boxColor = new GLColor(0, 0, .9f);
	private static final GLColor m_barrelColor = new GLColor(.5f, 0, 0);
	
	private static final Point3f m_basePos = new Point3f(0.0f, 0.0f, 0.0f);
	private static final Vector3f m_topPos = new Vector3f(0.0f, 1.6f, 0.0f);
	private static final Point3f m_boxPos = new Point3f(0.0f, 0.0f, 0.0f);
	private static final Vector3f m_barrelPos = new Vector3f(1.0f, 0.0f, 0.0f);
	
	public Vector3f getBarrelOrientation() {
		return m_barrelOrientation;
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
		gl.glRotatef(orientation.x, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(orientation.y, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(orientation.z, 0.0f, 0.0f, 1.0f);
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