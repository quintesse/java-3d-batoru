/*
 * Created on Nov 28, 2003
 */
package games.batoru.entities;

import javax.vecmath.Point3f;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.GLUquadric;
import net.java.games.jogl.util.GLUT;

import org.codejive.utils4gl.GLColor;
import org.codejive.utils4gl.RenderContext;
import org.codejive.world3d.Entity;
import org.codejive.world3d.Universe;

/**
 * @author tako
 * @version $Revision: 173 $
 */
public class TreeEntity extends Entity {
	static final GLColor m_trunkColor = new GLColor(.5f, .25f, 0);
	static final GLColor m_coneColor = new GLColor(0, .5f, .25f);
	int m_nTreeShapeList;
	
	public TreeEntity() {
		super();
	}
	
	public TreeEntity(Universe _universe, Point3f _position) {
		super(_universe, _position, 0.0f);
	}
	
	public void initRendering(RenderContext _context) {
		GL gl = _context.getGl();
		GLU glu = _context.getGlu();
		GLUT glut = _context.getGlut();

		m_nTreeShapeList = gl.glGenLists(1);
		gl.glNewList(m_nTreeShapeList, GL.GL_COMPILE);

		gl.glPushMatrix();
		gl.glColor3f(m_trunkColor.getRed(), m_trunkColor.getGreen(), m_trunkColor.getBlue());
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.2d, 0.2d, 1.6d, 6, 1);
		glu.gluDeleteQuadric(quad);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glColor3f(m_coneColor.getRed(), m_coneColor.getGreen(), m_coneColor.getBlue());
		gl.glTranslatef(0.0f, 1.6f, 0.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glut.glutSolidCone(glu, 0.7f, 4.0f, 6, 1);
		gl.glPopMatrix();

		gl.glEndList();		

		super.initRendering(_context);
	}
	
	public void render(RenderContext _context) {
		_context.getGl().glCallList(m_nTreeShapeList);
/*
		GL gl = _context.getGl();
		GLU glu = _context.getGlu();
		GLUT glut = _context.getGlut();

		gl.glPushMatrix();
		gl.glColor3f(m_trunkColor.getRed(), m_trunkColor.getGreen(), m_trunkColor.getBlue());
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.2d, 0.2d, 1.6d, 6, 1);
		glu.gluDeleteQuadric(quad);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glColor3f(m_coneColor.getRed(), m_coneColor.getGreen(), m_coneColor.getBlue());
		gl.glTranslatef(0.0f, 1.6f, 0.0f);
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		glut.glutSolidCone(glu, 0.7f, 4.0f, 6, 1);
		gl.glPopMatrix();
*/
	}
}

/*
 * $Log$
 * Revision 1.1  2003/12/01 22:49:26  tako
 * Class added because Entities now must know how render themselves.
 *
 */