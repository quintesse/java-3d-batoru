/*
 * Created on Sep 11, 2003
 */
package games.batoru.client;

import java.util.Iterator;

import games.batoru.entities.Player;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import net.java.games.jogl.GL;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.Renderable;
import org.codejive.world3d.Shape;
import org.codejive.world3d.Universe;

/**
 * @author Tako
 */
public class UniverseRenderer implements Renderable {
	private Universe m_universe;
	private Shape m_avatar;
	
	private boolean m_bReadyForRendering;
	
	public UniverseRenderer(RenderContext _context, Universe _universe, Shape _avatar) {
		m_universe = _universe;
		m_avatar = _avatar;

		m_bReadyForRendering = false;
	}
	
	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}
	
	public void initRendering(RenderContext _context) {
		m_avatar.initRendering(_context);
		m_bReadyForRendering = true;
	}
	
	public void render(RenderContext _context) {
		GL gl = _context.getGl();

//		Matrix3f orient = m_avatar.getOrientation();
//		if (drawable == m_canvas2) {
//			orient = new Matrix3f();
//			orient.sub(m_avatar.getOrientation());
//		}
//		gl.glLoadMatrixf(GLUtils.getGLMatrix(orient));

		gl.glTranslatef(0.0f, -0.0f, -5.0f);

		Vector3f orientation = m_avatar.getOrientation();
		gl.glRotatef(-orientation.x, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(-orientation.z, 0.0f, 0.0f, 1.0f);

		gl.glPushMatrix();
		m_avatar.render(_context);
		gl.glPopMatrix();

		gl.glRotatef(-orientation.y, 0.0f, 1.0f, 0.0f);

		Point3f pos = m_avatar.getPosition();
		gl.glTranslatef(-pos.x, -(pos.y + ((Player)m_avatar).getEyeHeight()), -pos.z);

		gl.glPushMatrix();
		Iterator i = m_universe.getRenderables();
		while (i.hasNext()) {
			Shape s = (Shape)i.next();
			if (s != m_avatar) {
				if (!s.readyForRendering()) {
					s.initRendering(_context);
				}
				gl.glPushMatrix();
				pos = s.getPosition();
				gl.glTranslatef(pos.x, pos.y, pos.z);
				s.render(_context);
				gl.glPopMatrix();
			}
		}
		gl.glPopMatrix();
	}		
}
