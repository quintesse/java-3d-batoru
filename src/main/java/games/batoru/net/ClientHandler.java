/*
 * Created on Sep 30, 2003
 */
package games.batoru.net;

import games.batoru.EntityBuilder;
import games.batoru.entities.PlayerEntity;
import games.batoru.server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.world3d.SurfaceInformation;
import org.codejive.world3d.Universe;
import org.codejive.world3d.net.*;

/**
 * @author Tako
 */
public class ClientHandler extends ConnectedMessagePort {
	private Server m_server;

	private MessagePacket m_message;
	ServerSocket m_tcpServer;
	Socket m_tcpSocket;
	private PlayerEntity m_avatar;
		
	private Vector3f m_tmpVec = new Vector3f();
	
	private static Logger logger = Logger.getLogger(ClientHandler.class.getName());
	
	public ClientHandler(Server _server, InetAddress _address, int _nPort) {
		super("ClientHandler", _address, _nPort);
		m_server = _server;
		m_message = new MessagePacket();
		m_avatar = null;
		start();
		setName("ClientHandler #" + getPort());
	}
	
	public PlayerEntity getAvatar() {
		return m_avatar;
	}

	public void setAvatar(PlayerEntity _avatar) {
		m_avatar = _avatar;
	}
	
	protected MessageStream requestConnection() {
		logger.info("asking remote client to set up a TCP connection");
		ServerMessageHelper.sendOpenTcp(m_message, this);
		
		try {
			m_tcpServer = new ServerSocket(getPort());
			m_tcpSocket = m_tcpServer.accept();
			logger.info("TCP connection established with remote client");
			MessageStream ms = new MessageStream(m_tcpSocket);
			return ms;
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
	}
	
	public void doInitialization() throws InterruptedException {
		logger.info("starting remote client initialization");

		// Wait for READY message
		logger.info("waiting for remote client READY");
		receivePacket();
		
		// Ask the client to open a TCP connection to us
		MessageStream out = requestConnection();
		
		// Tell the client which classes we're using
		ServerMessageHelper.addClasses(out);
	
		// Spawn the remote 3d view on the client
		Universe u = m_server.getUniverse();
		ServerMessageHelper.addSpawn(out, u);
	
		SurfaceInformation si = u.newSurfaceInformation();
		u.getSurfaceAt(0.0f, 0.0f, si);
		m_avatar = (PlayerEntity)EntityBuilder.createPlayer(u, new Point3f(0.0f, si.getHeight(), 40.0f), new Vector3f(0, 0, 1), 2.0f);
		//avatar.setAffectedByGravity(false);
	
		// Spawn the remote avatar
		ServerMessageHelper.addSpawn(out, m_avatar);
	
		// Start the 3d rendering loop on the client
		ServerMessageHelper.addStart3d(out, u, m_avatar);
		
		// Close the TCP connection because we don't need it anymore
		out.close();
	}

	@Override
	public void handlePacket(MessagePacket _packet) {
		super.handlePacket(_packet);
		while (_packet.hasMoreData()) {
			byte packetType = _packet.readByte();
			// Special packet
			switch (packetType) {
				case ClientMessageHelper.MSG_DISCONNECT:
					m_server.removeClient(this);
					logger.info("disconnected");
					stop();
					break;
				case ClientMessageHelper.MSG_MOVEMENT:
					logger.info("movement update received");
					m_tmpVec.set(_packet.readFloat(), _packet.readFloat(), _packet.readFloat());
					if (m_avatar != null) {
						m_avatar.getPosition().add(m_tmpVec);
					}
					break;
				case ClientMessageHelper.MSG_ORIENTATION:
					logger.info("orientation update received");
					if (m_avatar != null) {
						m_avatar.setOrientation(_packet.readFloat(), _packet.readFloat(), _packet.readFloat());
					}
					break;
				case ClientMessageHelper.MSG_STATE_FLAGS:
					logger.info("state flags received");
					byte nFlags = _packet.readByte();
					if ((nFlags & ClientMessageHelper.STATE_FIREPRIMARY) != 0) {
						logger.info("client holds down the primary fire button");
						EntityBuilder.createBullet(m_server.getUniverse(), m_avatar.getPosition(), m_avatar.getOrientation(), 20.0f, 5.0f);
// TODO This code shouldn't be here!!!!!!!!!! Testing purposes only!!!!
//						initPacket(m_message);
//						ServerMessageHelper.addSpawn(m_message, bullet);
//						sendPacket(m_message);
					}
					break;
				default:
					logger.info("unknown packet type received");
					break;
			}
		}
		releasePacket(_packet);
	}
}
