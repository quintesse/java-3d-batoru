/*
 * Created on 19-mrt-2003
 */
package games.batoru.shapes;

import java.awt.Color;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.GLUquadric;

import org.codejive.world3d.*;
import org.codejive.utils4gl.*;

/**
 * @author Tako
 */
public class BulletShape extends Shape implements Renderable {
	public static final int PART_TOP = 0;
	
	private static final GLColor m_bullet1Color = new GLColor(Color.ORANGE);
	private static final GLColor m_bullet2Color = new GLColor(Color.YELLOW);
	private static final GLColor m_bullet3Color = Colors.WHITE;
	
	public void render(RenderContext _context) {
		GL gl = _context.getGl();
		GLU glu = _context.getGlu();

		gl.glPushMatrix();
		gl.glColor3f(m_bullet1Color.getRed(), m_bullet1Color.getGreen(), m_bullet1Color.getBlue());
		gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluSphere(quad, 0.4d, 6, 6);
		glu.gluDeleteQuadric(quad);
		gl.glPopMatrix();
	}
}