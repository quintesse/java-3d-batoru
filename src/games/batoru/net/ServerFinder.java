/*
 * Created on Oct 3, 2003
 */
package games.batoru.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.codejive.world3d.*;
import org.codejive.world3d.net.*;

/**
 * @author Tako
 */
public class ServerFinder extends MessagePort {
	private LinkedList m_servers;
	private ServerlistChangedListener m_listchangeListener;
	
	public ServerFinder() {
		super("Server Finder", 22344);
		m_servers = new LinkedList();
		m_listchangeListener = null;
	}
	
	public List getServers() {
		return Collections.unmodifiableList(m_servers);
	}
	
	public interface ServerlistChangedListener {
		public void serverlistChanged(ServerFinder _finder);
	}
	
	public void setServerlistChangedListener(ServerlistChangedListener _listener) {
		m_listchangeListener = _listener;
	}
	
	private void notifyServerlistChangedListeners() {
		if (m_listchangeListener != null) {
			m_listchangeListener.serverlistChanged(this);
		}
	}
	
	public void handlePacket(MessagePacket _packet) {
		int nCookie = _packet.readInt();
		if (nCookie == ServerMessageHelper.MAGIC_COOKIE) {
			short nProtocolVersion = _packet.readShort();
			if (nProtocolVersion == ServerMessageHelper.PROTOCOL_VERSION) {
				byte inet_addr[] = new byte[4];
				_packet.readByteArray(inet_addr, 0, 4);
				try {
					// Read server info from packet
					InetAddress address = InetAddress.getByAddress(inet_addr);
					int nPort = _packet.readInt();
					String sServerName = _packet.readString();

					// Remember the info about the server we're connected to
					ServerInfo serverInfo = new ServerInfo(address, nPort, sServerName);
					m_servers.add(serverInfo);
					
					// Notify interested parties that hte list has changed
					notifyServerlistChangedListeners();
				} catch (UnknownHostException e) { /* ignore */ }
			} else {
				Universe.log(this, "unsupported server protocol version");
			}
		} else {
			Universe.log(this, "unknown server broadcast packet received");
		}
	}
}
