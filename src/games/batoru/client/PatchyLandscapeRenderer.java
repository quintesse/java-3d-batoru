package games.batoru.client;

import javax.vecmath.*;

import net.java.games.jogl.GL;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.Renderable;
import org.codejive.world3d.Universe;

import games.batoru.PatchyLandscape;

/**
 * @author Tako
 *
 */
public class PatchyLandscapeRenderer implements Renderable {
	private Universe m_universe;
	private PatchyLandscape m_model;
	
	private float m_fTimeOfBirth;
	private int m_nTerrainList;
	
	public PatchyLandscapeRenderer(Universe _universe, PatchyLandscape _model) {
		m_universe = _universe;
		m_model = _model;
		
		m_fTimeOfBirth = m_universe.getAge();
		m_nTerrainList = -1;
	}
	
	public boolean readyForRendering() {
		return (m_nTerrainList != -1);
	}
	
	public void initRendering(RenderContext _context) {
		updateRendering(_context);
	}
	
	public void updateRendering(RenderContext _context) {
		GL gl = _context.getGl();
		m_nTerrainList = gl.glGenLists(1);
		gl.glNewList(m_nTerrainList, GL.GL_COMPILE);
		renderGeo(gl);
		gl.glEndList();
	}
	
	public void render(RenderContext _context) {
		GL gl = _context.getGl();
		
		gl.glBindTexture(GL.GL_TEXTURE_2D, _context.getTextureHandle(0));
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		gl.glCallList(m_nTerrainList);
		gl.glEnd();
	}
	
	private void renderGeo(GL _gl) {
		float vfHeights[][] = m_model.getHeights();
		Point3f origin = m_model.getOrigin();
		boolean bSwitchDirection = false;
		int w = m_model.getWidth();
		int h = m_model.getHeight();
		for (int i = 0; i < w; i++) {
			if (bSwitchDirection) {
				for (int j = 0; j < h; j++) {
					_gl.glTexCoord2f((float)(i + 1) / w, (float)j / h);
					_gl.glVertex3f(origin.x + (i + 1) * m_model.getPatchWidth(), origin.y + vfHeights[i + 1][j], origin.z + j * m_model.getPatchHeight());
					_gl.glTexCoord2f((float)i / w, (float)j / h);
					_gl.glVertex3f(origin.x + i * m_model.getPatchWidth(), origin.y + vfHeights[i][j], origin.z + j * m_model.getPatchHeight());
				}
			} else {
				for (int j = h - 1; j >= 0; j--) {
					_gl.glTexCoord2f((float)i / w, (float)j / h);
					_gl.glVertex3f(origin.x + i * m_model.getPatchWidth(), origin.y + vfHeights[i][j], origin.z + j * m_model.getPatchHeight());
					_gl.glTexCoord2f((float)(i + 1) / w, (float)j / h);
					_gl.glVertex3f(origin.x + (i + 1) * m_model.getPatchWidth(), origin.y + vfHeights[i + 1][j], origin.z + j * m_model.getPatchHeight());
				}
			}
			bSwitchDirection = !bSwitchDirection;
		}
	}
}
