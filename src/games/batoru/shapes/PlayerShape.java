/*
 * Created on 24-mrt-2003
 */
package games.batoru.shapes;

import net.java.games.jogl.GL;

import org.codejive.world3d.*;
import org.codejive.world3d.loaders.md3.MD3Loader;
import org.codejive.world3d.net.MessageReader;
import org.codejive.world3d.net.NetworkDecoder;
import org.codejive.utils4gl.*;

import games.batoru.entities.Player;

/**
 * @author tako
 */
public class PlayerShape extends Shape implements Renderable, Player, NetworkDecoder {
	private float m_fEyeHeight;

	private MD3Loader m_lara;
	private int m_nLocomotionState;
	private int m_nActionState;
	
	public float getEyeHeight() {
		return m_fEyeHeight;
	}

	public void setLocomotionState(int _nState) {
		if (m_nLocomotionState != _nState) {
			switch (_nState) {
				case LOC_IDLE:
					m_lara.SetLegsAnimation("LEGS_IDLE");
					break;
				case LOC_WALKING:
					m_lara.SetLegsAnimation("LEGS_WALK");
					break;
				case LOC_RUNNING:
					m_lara.SetLegsAnimation("LEGS_RUN");
					break;
			}
			m_nLocomotionState = _nState;
		}
	}
	
	public void setActionState(int _nState) {
		if (m_nActionState != _nState) {
			switch (_nState) {
				case ACT_IDLE:
					m_lara.SetTorsoAnimation("TORSO_STAND");
					break;
				case ACT_SHOOTING:
					m_lara.SetTorsoAnimation("TORSO_ATTACK");
					break;
			}
			m_nActionState = _nState;
		}
	}

	public boolean isOnSurface() {
		return true;
	}

	public void initRendering(RenderContext _context) {
		m_lara = new MD3Loader(_context.getGl(), _context.getGlu());
		m_lara.LoadModel("models/lara", "lara");
		m_lara.LoadWeapon("models/lara", "railgun");
		m_nLocomotionState = -1;
		m_nActionState = -1;
		setLocomotionState(LOC_IDLE);
		setActionState(ACT_IDLE);
		super.initRendering(_context);
	}
	
	public void render(RenderContext _context) {
		GL gl = _context.getGl();
		gl.glTranslatef(0.0f, -1.4f, 0.0f);
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
		gl.glScalef(0.03f, 0.03f, 0.03f);
		gl.glColor3f(1, 1, 1);
		gl.glCullFace(GL.GL_FRONT);    // Quake3 uses front face culling apparently
		m_lara.DrawModel();
		gl.glCullFace(GL.GL_BACK);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.ConnectedMessagePort)
	 */
	public void netInit(MessageReader _reader) {
		super.netInit(_reader);
		m_fEyeHeight = _reader.readFloat();
	}
}
