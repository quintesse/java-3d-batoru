/*
 * Created on Sep 30, 2003
 */
package games.batoru.net;

import java.net.*;
import java.util.List;

import org.codejive.world3d.Universe;
import org.codejive.world3d.net.*;

/**
 * @author Tako
 */
public class ServerMessageHelper {
	public static final int MAGIC_COOKIE = 0x54616b4f;
	public static final short PROTOCOL_VERSION = 1;

	public static final byte MSG_CONNECT_ACCEPT = (byte)0x81;
	public static final byte MSG_CONNECT_DENY = (byte)0x82;
	public static final byte MSG_DISCONNECT = (byte)0x83;
	public static final byte MSG_OPEN_TCP = (byte)0x84;
	public static final byte MSG_CLASS_LIST = (byte)0x85;
	public static final byte MSG_SPAWN_ENTITY = (byte)0x86;
	public static final byte MSG_UPDATE_ENTITY = (byte)0x87;
	public static final byte MSG_KILL_ENTITY = (byte)0x88;
	public static final byte MSG_START_3D = (byte)0x89;
	
	public static void sendIThink(MessagePacket _packet, MessagePort _server) {
		try {
			_packet.clear();
			_packet.writeInt(MAGIC_COOKIE);
			_packet.writeShort(PROTOCOL_VERSION);
			_packet.writeByteArray(InetAddress.getLocalHost().getAddress(), 0, 4);
			_packet.writeInt(_server.getPort());
			_packet.writeString(_server.getName());
			Universe.log(ServerMessageHelper.class, "broadcasting server announcement");
			_server.sendBroadcastPacket(_packet);
		} catch (UnknownHostException e) {
			System.err.println(e);
		}
	}
	
	public static void sendConnectAccept(MessagePacket _packet, MessagePort _server, MessagePort _client) {
		_client.initPacket(_packet);
		_packet.writeInt(MAGIC_COOKIE);
		_packet.writeByte(MSG_CONNECT_ACCEPT);
		_packet.writeShort((short)_client.getPort());
		Universe.log(ServerMessageHelper.class, "sending CONNECT ACCEPT reply");
		_server.sendPacket(_packet.getAddress(), _packet.getPort(), _packet);
	}
	
	public static void sendConnectDeny(MessagePacket _packet, MessagePort _server, MessagePort _client, String _sReason) {
		_client.initPacket(_packet);
		_packet.writeInt(MAGIC_COOKIE);
		_packet.writeByte(MSG_CONNECT_DENY);
		_packet.writeString(_sReason);
		Universe.log(ServerMessageHelper.class, "sending CONNECT DENY reply");
		_server.sendPacket(_packet.getAddress(), _packet.getPort(), _packet);
	}
	
	public static void sendDisconnect(MessagePacket _packet, MessagePort _server, String _sReason) {
		_server.initPacket(_packet);
		_packet.writeByte(MSG_DISCONNECT);
		_packet.writeString(_sReason);
		Universe.log(ServerMessageHelper.class, "sending DISCONNECT");
		_server.sendPacket(_packet);
	}
	
	public static void sendOpenTcp(MessagePacket _packet, MessagePort _server) {
		_server.initPacket(_packet);
		_packet.writeByte(MSG_OPEN_TCP);
		Universe.log(ServerMessageHelper.class, "sending OPEN TCP");
		_server.sendPacket(_packet);
	}
	
	public static void addClasses(MessageWriter _writer) {
		_writer.writeByte(MSG_CLASS_LIST);
		Universe.log(ServerMessageHelper.class, "sending CLASS LIST");
		List classes = NetworkClassCache.getServerCache().getRegisteredClasses();
		for (int i = 0; i < classes.size(); i++) {
			_writer.writeString(NetworkClassCache.getServerCache().getClientClassName(i));
		}
		_writer.writeString("");
	}
	
	public static void addSpawn(MessageWriter _writer, short _nClassIndex, short _nInstanceId) {
		_writer.writeByte(MSG_SPAWN_ENTITY);
		_writer.writeShort(_nClassIndex);
		_writer.writeShort(_nInstanceId);
		Universe.log(ServerMessageHelper.class, "adding SPAWN ENTITY message for " + NetworkClassCache.getServerCache().getClientClassName(_nClassIndex) + " (#" + _nInstanceId + ")");
	}
	
	public static void addSpawn(MessageWriter _writer, NetworkEncoder _object) {
		addSpawn(_writer, _object.getClassIndex(), _object.getInstanceId());
		_object.writeInit(_writer);
	}
	
	public static void addUpdate(MessageWriter _writer, short _nInstanceId) {
		_writer.writeByte(MSG_UPDATE_ENTITY);
		_writer.writeShort(_nInstanceId);
		Universe.log(ServerMessageHelper.class, "adding UPDATE ENTITY message for #" + _nInstanceId);
	}
	
	public static void addUpdate(MessageWriter _writer, NetworkEncoder _object) {
		addUpdate(_writer, _object.getInstanceId());
		_object.writeInit(_writer);
	}
	
	public static void addKill(MessageWriter _writer, short _nInstanceId) {
		_writer.writeByte(MSG_KILL_ENTITY);
		_writer.writeShort(_nInstanceId);
		Universe.log(ServerMessageHelper.class, "adding KILL ENTITY message for #" + _nInstanceId);
	}
	
	public static void addKill(MessageWriter _writer, NetworkEncoder _object) {
		addKill(_writer, _object.getInstanceId());
		_object.writeInit(_writer);
	}
	
	public static void addStart3d(MessageWriter _writer, NetworkEncoder universe, NetworkEncoder avatar) {
		_writer.writeByte(MSG_START_3D);
		_writer.writeShort(universe.getInstanceId());
		_writer.writeShort(avatar.getInstanceId());
		Universe.log(ServerMessageHelper.class, "adding START 3D message");
	}
}
