/*
 * Created on 24-mrt-2003
 */
package games.batoru.entities;

import javax.vecmath.*;

import org.codejive.utils4gl.Vectors;
import org.codejive.world3d.*;
import org.codejive.world3d.net.MessageWriter;
import org.codejive.world3d.net.NetworkEncoder;

/**
 * @author tako
 */
public class PlayerEntity extends Entity implements Player, LiveEntity, NetworkEncoder {
	private float m_fEyeHeight; // meters above ground
	
	public PlayerEntity(Universe _universe, EntityClass _class, Point3f _position, Vector3f _orientation, float _fEyeHeight) {
		super(_universe, _class, _position, _orientation, Vectors.VECTF_ZERO, 1.0f);
		m_fEyeHeight = _fEyeHeight;
	}

	public float getEyeHeight() {
		return m_fEyeHeight;
	}
	
	public void setLocomotionState(int _nState) {
		// Unused on the client side
	}
	
	public void setActionState(int _nState) {
		// Unused on the client side
	}

	public void updateState() {
		super.updateState();
	}

	public void heartbeat(long _time) {
		// Unused on the client side
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeInit(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeInit(MessageWriter _writer) {
		super.writeInit(_writer);
		_writer.writeFloat(getEyeHeight());
	}
}
