/*
 * Created on Sep 11, 2003
 */
package games.batoru.client;

import java.util.Iterator;

import games.batoru.PatchyLandscape;
import games.batoru.entities.PlayerEntity;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.RenderObserver;
import org.codejive.utils4gl.Renderable;
import org.codejive.utils4gl.Vectors;
import org.codejive.world3d.Entity;
import org.codejive.world3d.LiveEntity;
import org.codejive.world3d.Universe;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

/**
 * @author Tako
 */
public class UniverseRenderer implements Renderable {
	private Universe m_universe;
	private PatchyLandscapeRenderer m_lrenderer;
	private Entity m_avatar;
	
	private boolean m_bReadyForRendering;
	
	public UniverseRenderer(Universe _universe, Entity _avatar) {
		m_universe = _universe;
		m_avatar = _avatar;

		m_bReadyForRendering = false;
	}
	
	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}
	
	public void initRendering(RenderContext _context) {
		m_lrenderer = new PatchyLandscapeRenderer(m_universe, (PatchyLandscape)m_universe.getLandscape());
		m_lrenderer.initRendering(_context);
		m_avatar.initRendering(_context);
		m_bReadyForRendering = true;
	}
	
	public void render(RenderContext _context, RenderObserver _observer) {
		GL2 gl = _context.getGl();
		GLU glu = _context.getGlu();

		Point3f pos = m_avatar.getPosition();
		Vector3f orientation = m_avatar.getOrientation();
		float fEye = ((PlayerEntity)m_avatar).getEyeHeight();
		if (false) {
			gl.glTranslatef(0.0f, -0.0f, -5.0f);
			gl.glPushMatrix();
			m_avatar.render(_context, _observer);
			gl.glPopMatrix();
		}
		glu.gluLookAt(0, 0, 0, orientation.x, orientation.y, orientation.z, 0, 1, 0);
		gl.glTranslatef(-pos.x, -(pos.y + fEye), -pos.z);
		
		{	// TODO Just here for testing purposes!!!!
			// The code here gives client-side objects a heartbeat
			// Normally this should only happen on the server!!
			Iterator<LiveEntity> i = m_universe.getLiveEntities();
			while (i.hasNext()) {
				LiveEntity e = i.next();
				e.heartbeat(m_universe.getAge());
				
			}
		}	// TODO Just here for testing purposes!!!!

		// Render the landscape
		m_lrenderer.render(_context, _observer);
		
		// Render the Entities
		renderObjects(_context, _observer);
	}		

	void renderObjects(RenderContext _context, RenderObserver _observer) {
		GL2 gl = _context.getGl();

		Iterator<Renderable> i = m_universe.getRenderables();
		while (i.hasNext()) {
			Renderable r = i.next();
			if (r != m_avatar) {
				if (!r.readyForRendering()) {
					r.initRendering(_context);
				}
				gl.glPushMatrix();
				Point3f pos;
				if (r instanceof Entity) {
					pos = ((Entity)r).getPosition();
				} else {
					pos = Vectors.POSF_CENTER;
				}
				gl.glTranslatef(pos.x, pos.y, pos.z);
				r.render(_context, _observer);
				gl.glPopMatrix();
			}
		}
	}
}
