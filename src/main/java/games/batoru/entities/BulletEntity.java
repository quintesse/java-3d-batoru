/*
 * Created on Nov 28, 2003
 */
package games.batoru.entities;

import java.awt.Color;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.utils4gl.GLColor;
import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.RenderObserver;
import org.codejive.world3d.Entity;
import org.codejive.world3d.Universe;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

/**
 * @author tako
 * @version $Revision: 363 $
 */
public class BulletEntity extends Entity {
//	private int m_nBulletShapeList;
	
	private static final GLColor m_bullet2Color = new GLColor(Color.YELLOW, 0.7f);
	
	public BulletEntity() {
		super();
	}

	public BulletEntity(Universe _universe, Point3f _position, Vector3f _impulse, float _fGravityFactor) {
		super(_universe, _position, _impulse, _fGravityFactor);
	}
	
	@Override
	public void initRendering(RenderContext _context) {
/*		GL gl = _context.getGl();
		GLU glu = _context.getGlu();
		
		m_nBulletShapeList = gl.glGenLists(1);
		gl.glNewList(m_nBulletShapeList, GL.GL_COMPILE);

		gl.glDepthMask(false);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);

		GLUquadric quad = glu.gluNewQuadric();
		glu.gluSphere(quad, 0.1d, 6, 6);
		glu.gluSphere(quad, 0.2d, 6, 6);
		glu.gluSphere(quad, 0.3d, 6, 6);
		glu.gluDeleteQuadric(quad);

		gl.glDisable(GL.GL_BLEND);
		gl.glDepthMask(true);

		gl.glEndList();		
*/
		super.initRendering(_context);
	}

	public void render(RenderContext _context, RenderObserver _observer) {
		GL2 gl = _context.getGl();
		GLU glu = _context.getGlu();

		gl.glColor4f(m_bullet2Color.getRed(), (float)Math.random(), m_bullet2Color.getBlue(), 0.5f);
//		_context.getGl().glCallList(m_nBulletShapeList);

		gl.glDepthMask(false);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

		GLUquadric quad = glu.gluNewQuadric();
		glu.gluSphere(quad, 0.1d, 6, 6);
		glu.gluSphere(quad, 0.2d, 6, 6);
		glu.gluSphere(quad, 0.3d, 6, 6);
		glu.gluDeleteQuadric(quad);

		gl.glDisable(GL2.GL_BLEND);
		gl.glDepthMask(true);
	}
}

/*
 * $Log$
 * Revision 1.1  2003/12/01 22:49:26  tako
 * Class added because Entities now must know how render themselves.
 *
 */
