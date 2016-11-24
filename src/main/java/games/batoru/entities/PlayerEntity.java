/*
 * Created on 24-mrt-2003
 */
package games.batoru.entities;

import javax.vecmath.*;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.RenderObserver;
import org.codejive.utils4gl.Vectors;
import org.codejive.world3d.*;
import org.codejive.world3d.loaders.md3.MD3Loader;
import org.codejive.world3d.net.MessageReader;
import org.codejive.world3d.net.MessageWriter;
import org.codejive.world3d.net.NetworkEncoder;

import com.jogamp.opengl.GL2;

/**
 * @author tako
 */
public class PlayerEntity extends Entity implements LiveEntity, NetworkEncoder {
	private float m_fEyeHeight; // meters above ground

	private MD3Loader m_lara;
	private int m_nLocomotionState;
	private int m_nActionState;
	
	public static final int LOC_IDLE = 0;
	public static final int LOC_WALKING = 1;
	public static final int LOC_RUNNING = 2;
	public static final int LOC_JUMPING = 3;
	public static final int LOC_LANDING = 4;
	
	public static final int ACT_IDLE = 0;
	public static final int ACT_SHOOTING = 1;

	public PlayerEntity() {
		super();
		m_bIsTargetable = true;
	}
	
	public PlayerEntity(Universe _universe, Point3f _position, Vector3f _orientation, float _fEyeHeight) {
		super(_universe, _position, _orientation, Vectors.VECTF_ZERO, 1.0f);
		m_fEyeHeight = _fEyeHeight;
		m_bIsTargetable = true;
	}

	public float getEyeHeight() {
		return m_fEyeHeight;
	}
	
	@Override
	public void updateState() {
		super.updateState();
	}

	public void heartbeat(float _fTime) {
		// Unused on the client side
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

	@Override
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
	
	public void render(RenderContext _context, RenderObserver _observer) {
		GL2 gl = _context.getGl();
		gl.glTranslatef(0.0f, -1.4f, 0.0f);
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
		gl.glScalef(0.03f, 0.03f, 0.03f);
		gl.glColor3f(1, 1, 1);
		gl.glCullFace(GL2.GL_FRONT);    // Quake3 uses front face culling apparently
		m_lara.DrawModel();
		gl.glCullFace(GL2.GL_BACK);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.ConnectedMessagePort)
	 */
	@Override
	public void netInit(MessageReader _reader) {
		super.netInit(_reader);
		m_fEyeHeight = _reader.readFloat();
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeInit(org.codejive.world3d.net.MessageWriter)
	 */
	@Override
	public void writeInit(MessageWriter _writer) {
		super.writeInit(_writer);
		_writer.writeFloat(getEyeHeight());
	}
}
