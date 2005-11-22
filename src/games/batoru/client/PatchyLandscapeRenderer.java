package games.batoru.client;

import javax.vecmath.*;

import javax.media.opengl.GL;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.RenderObserver;
import org.codejive.utils4gl.Renderable;
import org.codejive.utils4gl.geometries.Geometry;
import org.codejive.utils4gl.geometries.TriangleStripGeometry;
import org.codejive.utils4gl.geometries.VertexBuffer;
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
	private Geometry m_geo;
	
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
		GL gl = _context.getGl();
		
/*		m_nTerrainList = gl.glGenLists(1);
		gl.glNewList(m_nTerrainList, GL.GL_COMPILE);
		renderGeo(gl);
		gl.glEndList();
*/		
		m_geo = createGeo(gl);
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, m_geo.getBuffer().getVertices());
		// textures
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, m_geo.getBuffer().getTexCoords());
	}
	
	public void render(RenderContext _context, RenderObserver _observer) {
		GL gl = _context.getGl();
		
		_context.getTexture(0).bind();
//		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//		gl.glBegin(GL.GL_TRIANGLE_STRIP);
//		gl.glCallList(m_nTerrainList);
//		gl.glEnd();
		gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, m_geo.getBuffer().getSize());
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
	
	private Geometry createGeo(GL _gl) {
		float vfHeights[][] = m_model.getHeights();
		Point3f origin = m_model.getOrigin();
		boolean bSwitchDirection = false;
		int w = m_model.getWidth();
		int h = m_model.getHeight();
		int nFormat = VertexBuffer.COORDINATES | VertexBuffer.TEXTURES_2D | VertexBuffer.BUFFER_NIO;
		VertexBuffer vb = new VertexBuffer(2 * w * (h + 1), nFormat);
		for (int i = 0; i < w; i++) {
			if (bSwitchDirection) {
				for (int j = 0; j <= h; j++) {
					vb.addTexCoord((float)(i + 1) / w, (float)j / h);
					vb.addVertex(origin.x + (i + 1) * m_model.getPatchWidth(), origin.y + vfHeights[i + 1][j], origin.z + j * m_model.getPatchHeight());
					vb.addTexCoord((float)i / w, (float)j / h);
					vb.addVertex(origin.x + i * m_model.getPatchWidth(), origin.y + vfHeights[i][j], origin.z + j * m_model.getPatchHeight());
				}
			} else {
				for (int j = h; j >= 0; j--) {
					vb.addTexCoord((float)i / w, (float)j / h);
					vb.addVertex(origin.x + i * m_model.getPatchWidth(), origin.y + vfHeights[i][j], origin.z + j * m_model.getPatchHeight());
					vb.addTexCoord((float)(i + 1) / w, (float)j / h);
					vb.addVertex(origin.x + (i + 1) * m_model.getPatchWidth(), origin.y + vfHeights[i + 1][j], origin.z + j * m_model.getPatchHeight());
				}
			}
			bSwitchDirection = !bSwitchDirection;
		}
		return new TriangleStripGeometry(vb);
	}
}
