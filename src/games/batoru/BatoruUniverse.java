/*
 * Created on Nov 13, 2003
 */
package games.batoru;

import games.batoru.client.PatchyLandscapeRenderer;

import org.codejive.world3d.Universe;
import org.codejive.world3d.net.MessageReader;
import org.codejive.world3d.net.MessageWriter;

/**
 * @author tako
 */
public class BatoruUniverse extends Universe {
	
	// NetworkEncoder /////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeInit(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeInit(MessageWriter _writer) {
		super.writeInit(_writer);
		getLandscape().writeInit(_writer);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeKill(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeKill(MessageWriter _writer) {
		// TODO Handle Universe kill message writing
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeUpdate(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeUpdate(MessageWriter _writer) {
		// TODO Handle Universe update message writing
	}
	
	// NetworkDecoder /////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.MessageReader)
	 */
	public void netInit(MessageReader _reader) {
		super.netInit(_reader);
		PatchyLandscape landscape = new PatchyLandscape();
		setLandscape(landscape);
		landscape.netInit(_reader);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netKill(org.codejive.world3d.net.MessageReader)
	 */
	public void netKill(MessageReader _reader) {
		// TODO Handle Universe kill message writing
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netUpdate(org.codejive.world3d.net.MessageReader)
	 */
	public void netUpdate(MessageReader _reader) {
		// TODO Handle Universe update message writing
	}

}
