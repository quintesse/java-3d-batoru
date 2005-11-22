/*
 * Created on 13-mrt-2003
 */
package games.batoru;

import games.batoru.net.ClientMessageHelper;
import games.batoru.net.ServerMessageHelper;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import javax.vecmath.*;

import org.codejive.world3d.*;
import org.codejive.world3d.PhysicalEntity;
import org.codejive.world3d.SurfaceInformation;
import org.codejive.world3d.Universe;
import org.codejive.world3d.net.*;
import org.codejive.utils4gl.*;
import org.codejive.utils4gl.geometries.*;

/**
 * @author Tako
 */
public class PatchyLandscape implements NetworkEncoder, NetworkDecoder, Landscape {
	private int m_nWidth, m_nHeight;
	private float m_fPatchWidth, m_fPatchHeight;
	private Point3f m_origin;
	private long m_lSeed;
	
	private Vector3d m_downVector;
	private Random m_random;
	
	private short m_nClassIndex;
	private short m_nIstanceId;

	public class LandscapePatchType {
		private GLColor m_color;
		private float m_fFriction;
		
		LandscapePatchType(GLColor _color, float _fFriction) {
			m_color = _color;
			m_fFriction = _fFriction;
		}
		
		public GLColor getColor() {
			return m_color;
		}
		
		public float getFriction() {
			return m_fFriction;
		}
	}
	
	public final LandscapePatchType green = new PatchyLandscape.LandscapePatchType(new GLColor(Color.GREEN), 0.1f);
	public final LandscapePatchType dgreen = new PatchyLandscape.LandscapePatchType(new GLColor(Color.GREEN.darker()), 0.1f);
	public final LandscapePatchType ddgreen = new PatchyLandscape.LandscapePatchType(new GLColor(Color.GREEN.darker().darker()), 0.1f);
	public final LandscapePatchType dddgreen = new PatchyLandscape.LandscapePatchType(new GLColor(Color.GREEN.darker().darker().darker()), 0.1f);
	public final LandscapePatchType red = new PatchyLandscape.LandscapePatchType(new GLColor(Color.RED), 0.5f);
	
	public class LandscapePatch {
		private LandscapePatchType m_type;
		private PhysicalEntity m_object;
		
		LandscapePatch(LandscapePatchType _type) {
			m_type = _type;
		}
		
		public LandscapePatchType getType() {
			return m_type;
		}
		
		public void setType(LandscapePatchType _type) {
			m_type = _type;
		}
		
		public PhysicalEntity getObject() {
			return m_object;
		}
		
		public void setObject(PhysicalEntity _object) {
			m_object = _object;
		}
	}

	private float m_vfHeights[][];
	private LandscapePatch m_vPatches[][];

	public PatchyLandscape() {
		m_nClassIndex = NetworkClassCache.getClientCache().getClassIndex(this.getClass().getName());
		m_nIstanceId = NetworkInstanceIdGenerator.getNewId();
	}
	
	public PatchyLandscape(int _nWidth, int _nHeight, float _fPatchWidth, float _fPatchHeight, Point3f _origin, long _lSeed) {
		this();
		init(_nWidth, _nHeight, _fPatchWidth, _fPatchHeight, _origin, _lSeed);
	}
	
	public PatchyLandscape(int _nWidth, int _nHeight, float _fPatchWidth, float _fPatchHeight, long _lSeed) {
		this(_nWidth, _nHeight, _fPatchWidth, _fPatchHeight, new Point3f(-_nWidth * _fPatchWidth / 2, 0, -_nHeight * _fPatchHeight / 2), _lSeed);
	}
	
	public PatchyLandscape(int _nWidth, int _nHeight, float _fPatchWidth, float _fPatchHeight) {
		this(_nWidth, _nHeight, _fPatchWidth, _fPatchHeight, System.currentTimeMillis());
	}
	
	private void init(int _nWidth, int _nHeight, float _nPatchWidth, float _nPatchHeight, Point3f _origin, long _lSeed) {
		m_nWidth = _nWidth;
		m_nHeight = _nHeight;
		m_fPatchWidth = _nPatchWidth;
		m_fPatchHeight = _nPatchHeight;
		m_origin = _origin;
		m_downVector = new Vector3d(0, -1, 0);
		m_lSeed = _lSeed;
		
		m_random = new Random(_lSeed);
		
		// Allocate height map
		m_vfHeights = new float[getWidth() + 1][];
		for (int i = 0; i <= getWidth(); i++) {
			m_vfHeights[i] = new float[getHeight() + 1];
		}
		
		// Perform basic landscape designing
		for (int i = 0; i < 10; i++) {
			tectonics();
		}

		// Add noise
/*		for (int i = 0; i <= getWidth(); i++) {
			for (int j = 0; j <= getHeight(); j++) {
				if ((i == 0) || (i == getWidth()) || (j == 0) || (j == getWidth())) {
					m_vfHeights[i][j] = 0.0f;
				} else {
					m_vfHeights[i][j] += (float)(m_random.nextDouble() * 2) - 1.0f;
				}
			}
		}
*/		
		LandscapePatchType vTerrainTypes[] = {
			green,
			dgreen,
			ddgreen,
			dddgreen
		};
		
		m_vPatches = new LandscapePatch[getWidth()][];
		for (int i = 0; i < getWidth(); i++) {
			m_vPatches[i] = new LandscapePatch[getHeight()];
			for (int j = 0; j < getHeight(); j++) {
				int nTypeIdx = (int)(m_random.nextDouble() * vTerrainTypes.length);
				LandscapePatch patch = new LandscapePatch(vTerrainTypes[nTypeIdx]);
				m_vPatches[i][j] = patch;
			}
		}
		
		Universe.log(this, "Created landscape: " + m_nWidth + "x" + m_nHeight + " (seed #" + m_lSeed + ")");
	}
	
	private void tectonics() {
		int width = (int)(m_random.nextDouble() * (getWidth() + 1));
		int height = (int)(m_random.nextDouble() * (getHeight() + 1));
		int x = (int)(m_random.nextDouble() * (getWidth() + 1 - width));
		int y = (int)(m_random.nextDouble() * (getHeight() + 1 - height));
		float h = (float)(m_random.nextDouble() * 30) - 15.0f;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				m_vfHeights[x + i][y + j] += h;
			}
		}
	}
	
	public int getWidth() {
		return m_nWidth;
	}
	
	public int getHeight() {
		return m_nHeight;
	}
	
	public float getPatchWidth() {
		return m_fPatchWidth;
	}
	
	public float getPatchHeight() {
		return m_fPatchHeight;
	}
	
	public Point3f getOrigin() {
		return m_origin;
	}
	
	public float[][] getHeights() {
		return m_vfHeights;
	}
	
	public LandscapePatch[][] getPatches() {
		return m_vPatches;
	}
	
	public boolean getPatchIndex(Point2f _pos, Point _result) {
		return getPatchIndex(_pos.x, _pos.y, _result);
	}
	
	public boolean getPatchIndex(float _x, float _y, Point _result) {
		int x = (int)((_x - m_origin.x) / getPatchWidth());
		int y = (int)((_y - m_origin.z) / getPatchHeight());
		_result.move(x, y);
		return ((x >= 0) && (x < getWidth()) && (y >= 0) && (y < getHeight()));
	}
	
	public void getPatchPosition(Point _pos, Point3f _result) {
		float x = m_origin.x + (_pos.x + 0.5f) * getPatchWidth();
		float z = m_origin.z + (_pos.y + 0.5f) * getPatchHeight();
		PatchSurfaceInformation si = new PatchSurfaceInformation();
		getSurfaceAt(x, z, si);
		float y = m_origin.y + si.getHeight();
		_result.set(x, y, z);
	}
	
	public LandscapePatch getPatch(Point _pos) {
		return getPatches()[_pos.x][_pos.y];
	}
	
	public class PatchSurfaceInformation extends SurfaceInformation {
		private LandscapePatch m_patch;
		
		public LandscapePatch getPatch() {
			return m_patch;
		}
		
		public void setPatch(LandscapePatch _patch) {
			m_patch = _patch;
		}
	}
	
	public SurfaceInformation createSurfaceInformation() {
		return new PatchSurfaceInformation();
	}

	public void getSurfaceAt(float _x, float _y, SurfaceInformation _info) {
		Point patchPos = new Point();
		if (getPatchIndex(_x, _y, patchPos)) {
			float vfHeights[][] = getHeights();
			Point3f origin = getOrigin();

/*
			VertexBuffer vb = new DefaultVertexBuffer(6, VertexBuffer.COORDINATES);
	
			vb.addVertex(origin.x + patchPos.x * getPatchWidth(), origin.y + vfHeights[patchPos.x][patchPos.y], origin.z + patchPos.y * getPatchHeight());
			vb.addVertex(origin.x + patchPos.x * getPatchWidth(), origin.y + vfHeights[patchPos.x][patchPos.y + 1], origin.z + (patchPos.y + 1) * getPatchHeight());
			vb.addVertex(origin.x + (patchPos.x + 1) * getPatchWidth(), origin.y + vfHeights[patchPos.x + 1][patchPos.y + 1], origin.z + (patchPos.y + 1) * getPatchHeight());
			
//			vb.addVertex(origin.x + patchPos.x * getPatchWidth(), origin.y + vfHeights[patchPos.x][patchPos.y], origin.z + patchPos.y * getPatchHeight());
			vb.addVertex(origin.x + (patchPos.x + 1) * getPatchWidth(), origin.y + vfHeights[patchPos.x + 1][patchPos.y], origin.z + patchPos.y * getPatchHeight());
//			vb.addVertex(origin.x + (patchPos.x + 1) * getPatchWidth(), origin.y + vfHeights[patchPos.x + 1][patchPos.y + 1], origin.z + (patchPos.y + 1) * getPatchHeight());
*/
			VertexBuffer vb = new VertexBuffer(4, 4, VertexBuffer.COORDINATES | VertexBuffer.BUFFER_ARRAY);
			
			vb.addVertex(origin.x + patchPos.x * getPatchWidth(), origin.y + vfHeights[patchPos.x][patchPos.y], origin.z + patchPos.y * getPatchHeight());
			vb.addVertex(origin.x + (patchPos.x + 1) * getPatchWidth(), origin.y + vfHeights[patchPos.x + 1][patchPos.y], origin.z + patchPos.y * getPatchHeight());
			vb.addVertex(origin.x + patchPos.x * getPatchWidth(), origin.y + vfHeights[patchPos.x][patchPos.y + 1], origin.z + (patchPos.y + 1) * getPatchHeight());
			vb.addVertex(origin.x + (patchPos.x + 1) * getPatchWidth(), origin.y + vfHeights[patchPos.x + 1][patchPos.y + 1], origin.z + (patchPos.y + 1) * getPatchHeight());

			vb.addIndex(0);
			vb.addIndex(2);
			vb.addIndex(1);
			vb.addIndex(3);
			
			Geometry geom = new TriangleStripGeometry(vb);
			
			Point3d p = new Point3d(_x, 1000.0, _y);
			Intersection intersection = geom.intersectClosest(p, m_downVector);
			if (intersection.isIntersecting()) {
				_info.setHeight((float)intersection.getPoint().y);
				_info.getNormal().set(intersection.getNormal());
			} else {
				// No intersection???
				_info.setHeight(0.0f);
				_info.setNormal(Vectors.VECTF_ZERO);
			}
			if (_info instanceof PatchSurfaceInformation) {
				((PatchSurfaceInformation)_info).setPatch(getPatch(patchPos));
			}
		} else {
			_info.setHeight(0.0f);
			_info.setNormal(Vectors.VECTF_ZERO);
			if (_info instanceof PatchSurfaceInformation) {
				((PatchSurfaceInformation)_info).setPatch(null);
			}
		}
	}
	
	public void getSurfaceAt(Point2f _pos, SurfaceInformation _info) {
		getSurfaceAt(_pos.x, _pos.y, _info);
	}
	
	// NetworkEncoder /////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#getClassIndex()
	 */
	public short getClassIndex() {
		return m_nClassIndex;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#getInstanceId()
	 */
	public short getInstanceId() {
		return m_nIstanceId;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeInit(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeInit(MessageWriter _writer) {
		_writer.writeShort((short)m_nWidth);
		_writer.writeShort((short)m_nHeight);
		_writer.writeFloat(m_fPatchWidth);
		_writer.writeFloat(m_fPatchHeight);
		_writer.writeFloat(m_origin.x);
		_writer.writeFloat(m_origin.y);
		_writer.writeFloat(m_origin.z);
		_writer.writeLong(m_lSeed);

		// Count the landscape objects
		short nCnt = 0;
		PatchyLandscape.LandscapePatch vPatches[][] = getPatches();
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (vPatches[i][j].getObject() != null) {
					nCnt++;
				}
			}
		}
		_writer.writeShort(nCnt);

		// Spawn the remote landscape objects
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				PatchyLandscape.LandscapePatch patch = vPatches[i][j];
				NetworkEncoder entenc = (NetworkEncoder)patch.getObject();
				if (entenc != null) {
					ServerMessageHelper.addSpawn(_writer, entenc);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeUpdate(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeUpdate(MessageWriter _writer) {
		// Landscape's don't dig updating
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeKill(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeKill(MessageWriter _writer) {
		// Landscape's aren't into killing
	}

	// NetworkDecoder /////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.MessageReader)
	 */
	public void netInit(MessageReader _reader) {
		int nWidth = _reader.readShort();
		int nHeight = _reader.readShort();
		float fPatchWidth = _reader.readFloat();
		float fPatchHeight = _reader.readFloat();
		Point3f origin = new Point3f(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		long seed = _reader.readLong();
		init(nWidth, nHeight, fPatchWidth, fPatchHeight, origin, seed);

		int nCnt = _reader.readShort();
		Point pos = new Point();
		while (nCnt-- > 0) {
			_reader.readByte(); // SPAWN ENTITY message type which we ignore
			PhysicalEntity obj = (PhysicalEntity)ClientMessageHelper.spawn(_reader);
			getPatchIndex(obj.getPosition().x, obj.getPosition().y, pos);
			LandscapePatch patch = getPatch(pos);
			patch.setObject(obj);
		}
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netKill(org.codejive.world3d.net.MessageReader)
	 */
	public void netKill(MessageReader _reader) {
		// Landscape's aren't into killing
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netUpdate(org.codejive.world3d.net.MessageReader)
	 */
	public void netUpdate(MessageReader _reader) {
		// Landscape's don't dig updating
	}
}
