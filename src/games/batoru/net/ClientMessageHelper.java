/*
 * Created on Sep 30, 2003
 */
package games.batoru.net;

import javax.vecmath.*;

import org.codejive.world3d.Universe;
import org.codejive.world3d.net.*;

/**
 * @author Tako
 */
public class ClientMessageHelper {
	public static final byte MSG_CONNECT_REQUEST = (byte)0x81;
	public static final byte MSG_DISCONNECT = (byte)0x82;
	public static final byte MSG_READY = (byte)0x83;
	public static final byte MSG_MOVEMENT = (byte)0x84;
	public static final byte MSG_ORIENTATION = (byte)0x85;
	public static final byte MSG_STATE_FLAGS = (byte)0x86;
	
	public static final byte STATE_FIREPRIMARY = (byte)0x01;
	
	public static void sendConnectRequest(MessagePacket _packet, MessagePort _client) {
		_packet.clear();
		_packet.writeByte(MSG_CONNECT_REQUEST);
		Universe.log(_client, "sending CONNECT REQUEST");
		_client.sendPacket(_client.getDestinationAddress(), _client.getDestinationPort(), _packet);
	}
	
	public static void sendDisconnectRequest(MessagePacket _packet, MessagePort _client) {
		_client.initPacket(_packet);
		_packet.writeByte(MSG_DISCONNECT);
		Universe.log(_client, "sending DISCONNECT message");
		_client.sendPacket(_packet);
	}
	
	public static void sendReady(MessagePacket _packet, MessagePort _client) {
		_client.initPacket(_packet);
		_packet.writeByte(MSG_READY);
		Universe.log(_client, "sending READY message");
		_client.sendPacket(_packet);
	}
	
	public static void addMovement(MessageWriter _writer, Vector3f _movement) {
		_writer.writeByte(MSG_MOVEMENT);
		_writer.writeFloat(_movement.x);
		_writer.writeFloat(_movement.y);
		_writer.writeFloat(_movement.z);
	}
	
	public static void addOrientation(MessageWriter _writer, Vector3f _orientation) {
		_writer.writeByte(MSG_ORIENTATION);
		_writer.writeFloat(_orientation.x);
		_writer.writeFloat(_orientation.y);
		_writer.writeFloat(_orientation.z);
	}
	
	public static void addStateFlags(MessageWriter _writer, boolean _bFirePrimary) {
		byte nFlags = 0;
		if (_bFirePrimary) {
			nFlags |= STATE_FIREPRIMARY;
		}
		_writer.writeByte(MSG_STATE_FLAGS);
		_writer.writeByte(nFlags);
	}
	
	public static NetworkDecoder spawn(MessageReader _reader) {
		NetworkDecoder obj = null;
		short nClassIndex = _reader.readShort();
		short nInstanceId = _reader.readShort();
		String sClassName = NetworkClassCache.getClientCache().getClientClassName(nClassIndex);
		try {
			Class cls = Class.forName(sClassName);
			obj = (NetworkDecoder)cls.newInstance();
			NetworkClassCache.getClientCache().registerInstance(nInstanceId, obj);
			obj.netInit(_reader);
			Universe.log(ClientMessageHelper.class, "instantiated: " + sClassName + " (# " + nInstanceId + ")");
		} catch (Exception e) {
			Universe.log(ClientMessageHelper.class, "could not instantiate: " + sClassName + " because: " + e.getMessage());
		}
		return obj;
	}
	
	public static void update(MessageReader _reader) {
		short nInstanceId = _reader.readShort();
		NetworkDecoder obj = NetworkClassCache.getClientCache().getInstance(nInstanceId);
		obj.netUpdate(_reader);
		Universe.log(ClientMessageHelper.class, "updated # " + nInstanceId);
	}
	
	public static void kill(MessageReader _reader) {
		short nInstanceId = _reader.readShort();
		NetworkDecoder obj = NetworkClassCache.getClientCache().getInstance(nInstanceId);
		obj.netKill(_reader);
		Universe.log(ClientMessageHelper.class, "killed # " + nInstanceId);
	}
}
