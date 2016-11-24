/*
 * Created on Sep 30, 2003
 */
package games.batoru.net;

import java.net.InetAddress;

/**
 * @author Tako
 */
public class ServerInfo {
	private InetAddress m_serverAddress;
	private int m_nServerPort;
	private String m_sServerName;
	
	public ServerInfo(InetAddress _address, int _nPort, String _sName) {
		m_serverAddress = _address;
		m_nServerPort = _nPort;
		m_sServerName = _sName;
	}
	
	public InetAddress getAddress() {
		return m_serverAddress;
	}
		
	public int getPort() {
		return m_nServerPort;
	}
	
	public String getName() {
		return m_sServerName;
	}
}
