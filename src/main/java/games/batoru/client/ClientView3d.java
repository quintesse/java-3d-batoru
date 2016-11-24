/*
 * Created on Oct 1, 2003
 */
package games.batoru.client;

import games.batoru.EntityBuilder;
import games.batoru.entities.PlayerEntity;
import games.batoru.net.ClientMessageHelper;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.NumberFormat;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.codejive.gui4gl.events.GuiActionEvent;
import org.codejive.gui4gl.events.GuiActionListener;
import org.codejive.gui4gl.events.GuiKeyAdapter;
import org.codejive.gui4gl.events.GuiKeyEvent;
import org.codejive.gui4gl.events.GuiKeyListener;
import org.codejive.gui4gl.events.GuiMouseEvent;
import org.codejive.gui4gl.events.GuiMouseListener;
import org.codejive.gui4gl.themes.Theme;
import org.codejive.gui4gl.widgets.Button;
import org.codejive.gui4gl.widgets.Screen;
import org.codejive.gui4gl.widgets.Text;
import org.codejive.gui4gl.widgets.Window;
import org.codejive.utils4gl.FrameRateCounter;
import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.SimpleFrameRateCounter;
import org.codejive.utils4gl.Vectors;
import org.codejive.utils4gl.textures.Texture;
import org.codejive.utils4gl.textures.TextureReader;
import org.codejive.world3d.Camera;
import org.codejive.world3d.Entity;
import org.codejive.world3d.Universe;
import org.codejive.world3d.net.MessagePacket;
import org.codejive.world3d.net.MessagePort;
import org.codejive.world3d.net.MessageReader;
import org.codejive.world3d.net.NetworkDecoder;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * @author Tako
 * @version $Revision: 363 $
 */
public class ClientView3d implements NetworkDecoder {
	private String m_sTitle;
	private int m_nWidth, m_nHeight;
	private boolean m_bFullscreen;

	private GLWindow m_glWindow;
	private Animator m_animator;
	
	private static final int DEFAULT_WIDTH = 1024;
	private static final int DEFAULT_HEIGHT = 768;

	public ClientView3d(String _sTitle, boolean _bFullscreen) {
		this(_sTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT, _bFullscreen);
	}
	
	public ClientView3d(String _sTitle, int _nWidth, int _nHeight, boolean _bFullscreen) {
		m_sTitle = _sTitle;
		m_nWidth = _nWidth;
		m_nHeight = _nHeight;
		m_bFullscreen = _bFullscreen;
	}

	public void start() {
        Display display = NewtFactory.createDisplay(null);
        com.jogamp.newt.Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL2);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        m_glWindow = GLWindow.create(screen, glCapabilities);

        m_glWindow.setSize(m_nWidth, m_nHeight);
        m_glWindow.setPosition(50, 50);
        m_glWindow.setUndecorated(false);
        m_glWindow.setAlwaysOnTop(false);
        m_glWindow.setFullscreen(m_bFullscreen);
        m_glWindow.setPointerVisible(false);
        m_glWindow.confinePointer(true);
        m_glWindow.setTitle(m_sTitle);

        m_glWindow.setVisible(true);

		ClientViewRenderer renderer = new ClientViewRenderer(this, m_glWindow);
        m_glWindow.addGLEventListener(renderer);

        m_animator = new Animator(m_glWindow);
        m_animator.start();		
	}
	
	public void stop() {
		if (m_animator != null) {
			m_animator.stop();
			m_animator = null;
		}
	}
	
	public void netInit(MessageReader _reader) {
		start();
	}
	
	public void netUpdate(MessageReader _reader) {
		// Object does not support updates
	}
	
	public void netKill(MessageReader _reader) {
		stop();
	}
}

class ClientViewRenderer implements GLEventListener, GuiMouseListener, GuiKeyListener {
	private ClientView3d m_view;
	private GLWindow m_glWindow;
	private Universe m_universe;
	private Entity m_avatar;
	private Camera m_avatarCamera;
	private UniverseRenderer m_universeRenderer;

	private RenderContext m_context;
	private Screen m_screen;
	protected MenuWindow m_menuWindow;
	protected InfoWindow m_infoWindow;
	private FrameRateCounter m_frameRateCounter;
	
	private Robot m_aRobot;
	private boolean m_bGrabMouse;
	
	private boolean m_bKeyLeft = false;
	private boolean m_bKeyRight = false;
	private boolean m_bKeyForward = false;
	private boolean m_bKeyBackward = false;	private boolean m_bKeyJump = false;
	private boolean m_bFirePrimary = false;

	private float m_fRotX = 0.0f;
	private float m_fRotY = 0.0f;

	private float m_fSurfaceSpeed = 180.0f;	// meters per second
	private float m_fAirSpeed = 90.0f;	// meters per second

	private final Vector3f VECTF_JUMP = new Vector3f(0.0f, 15.0f, 0.0f);
	
	// These are only here to supposedly speed things up a bit	
	private Matrix3f m_viewMatrix = new Matrix3f();
	private Matrix3f m_tempMatrix = new Matrix3f();

	private float m_fLastAge = 0;
	private float m_fLastUpdate = 0;

	private MessagePort m_serverPort;
	private MessagePacket m_message;
	
	public ClientViewRenderer(ClientView3d _view, GLWindow _glWindow) {
		m_view = _view;
		m_glWindow = _glWindow;
//		m_universe = _client.getUniverse();
//		m_avatar = _client.getAvatar();
		m_avatarCamera = new Camera();
		m_universeRenderer = null;
		
//		m_serverPort = _client.getMessagePort();
		m_message = new MessagePacket();
//		m_serverPort.initPacket(m_message);
	}
		
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
//		GLU glu = new GLU();

		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glShadeModel(GL2.GL_SMOOTH);              // Enable Smooth Shading
//		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
//		gl.glClearDepth(1.0f);                      // Depth Buffer Setup
		gl.glEnable(GL2.GL_DEPTH_TEST);				// Enables Depth Testing
		gl.glDepthFunc(GL2.GL_LEQUAL);				// The Type Of Depth Testing To Do
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);	// Really Nice Perspective Calculations
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_NORMALIZE);
			
		// Set up lighting
		float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
		float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
		gl.glEnable(GL2.GL_LIGHT1);
//		gl.glEnable(GL.GL_LIGHTING);

		m_context = new RenderContext(gl);
		prepareTexture(m_context, 0, "games/batoru/textures/grass_03.jpg");

		Theme.setDefaultTheme(m_context);
		m_screen = new Screen();
		m_menuWindow = new MenuWindow();
		m_screen.add(m_menuWindow);
		m_infoWindow = new InfoWindow();
		m_infoWindow.setVisible(true);
		m_screen.add(m_infoWindow);

		m_glWindow.addKeyListener(m_screen);
		m_glWindow.addMouseListener(m_screen);
		m_screen.addKeyListener(this);
		m_screen.addMouseListener(this);
		
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_NEAREST);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_NEAREST);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

//		m_universeRenderer = new UniverseRenderer(m_universe, m_avatar);
//		m_universeRenderer.initRendering(m_context);

		m_frameRateCounter = new SimpleFrameRateCounter();
		
		m_screen.initRendering(m_context);
		
		// Lower the priority of the render thread so we don't affect the other threads (much)
		Thread.currentThread().setPriority(Thread.currentThread().getPriority() - 1);
	}

	public void dispose(GLAutoDrawable drawable) {
		System.exit(0);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLUgl2();

		float h = (float) width / (float) height;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);

		System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
		System.err.println();
		System.err.println("glLoadTransposeMatrixf() supported: " + gl.isFunctionAvailable("glLoadTransposeMatrixf"));
		if (!gl.isFunctionAvailable("glLoadTransposeMatrixf")) {
			// --- not using extensions
			gl.glLoadIdentity();
		} else {
			// --- using extensions
			final float[] identityTranspose = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
			gl.glLoadTransposeMatrixf(identityTranspose, 0);
		}
		glu.gluPerspective(45.0f, h, 0.5, 600.0);
//		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 600.0f);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		m_screen.initRendering(m_context);
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		m_frameRateCounter.addFrame();

		handleUniverseFrame();
			
		gl.glLoadIdentity();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glPushMatrix();
			
		m_universeRenderer.render(m_context, m_avatarCamera);
			
		gl.glPopMatrix();

		renderFrameRate(m_context, m_frameRateCounter.getFrameRate());
		updateInfo(m_frameRateCounter.getFrameRate());
		
		m_screen.render(m_context, null);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// Not needed
	}


	private void renderFrameRate(RenderContext _context, float _fFps) {
		GL2 gl = _context.getGl().getGL2();

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(1);
		Float f = new Float(_fFps);

		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix ();
		gl.glLoadIdentity();
 
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		_context.getGlu().gluOrtho2D(0, viewport[2], viewport[3], 0);
		gl.glDepthFunc(GL2.GL_ALWAYS);

		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(15, 15);
		_context.getGlut().glutBitmapString(GLUT.BITMAP_HELVETICA_18, "FPS: " + nf.format(f));
 
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix (); 		
	}

	protected void handleUniverseFrame() {
		float fCurrentAge = m_universe.getAge();
		if (m_fLastAge > 0) {
			float fElapsedTime = fCurrentAge - m_fLastAge;
			m_universe.handleFrame(fElapsedTime);
			handleAvatarFrame(fCurrentAge);
		}
		m_fLastAge = fCurrentAge;
	}
	
	protected void handleAvatarFrame(float _fCurrentAge) {
		float fElapsedTime = _fCurrentAge - m_fLastAge;

		int nXMovement = 0;
		int nZMovement = 0;

		// Depending on the left and right keys the X movement is -1, 1 or 0
		nXMovement += (m_bKeyLeft) ? -1 : 0;
		nXMovement += (m_bKeyRight) ? 1 : 0;
		// Depending on the forward and backward keys the Z movement is -1, 1 or 0
		nZMovement += (m_bKeyForward) ? -1 : 0;
		nZMovement += (m_bKeyBackward) ? 1 : 0;

		PlayerEntity avatar = (PlayerEntity)m_avatar;

		// Check if the player wants to jump and if it is possible
		float fGravFactor = avatar.getGravityFactor();
		if (m_bKeyJump && (Math.abs(fGravFactor) > Universe.ALMOST_ZERO) && avatar.isOnSurface()) {
			Vector3f jump = new Vector3f(avatar.getImpulse());
			jump.add(VECTF_JUMP);
			avatar.setImpulse(jump);
			avatar.updateState();
		}

		// Create the base movement vector
		Vector3f movement = new Vector3f(nXMovement, 0, nZMovement);
		if (movement.length() > 0.0) {
			// Make sure it's length is 1.0
			movement.normalize();
			// Scale the vector according to speed and elapsed time
			float fSpeed = (avatar.isOnSurface()) ? m_fSurfaceSpeed : m_fAirSpeed;
			movement.scale(fSpeed / 10 * fElapsedTime);		// In Java3D a unit is 10m

			// Rotate the movement vector so its translation is relative to the view port 
			m_viewMatrix.rotY((float)(m_fRotY / 180.0f * Math.PI));
			if (Math.abs(fGravFactor) <= Universe.ALMOST_ZERO) {
				m_tempMatrix.rotX((float)(m_fRotX / 180.0f * Math.PI));
				m_viewMatrix.mul(m_tempMatrix);
			}
			m_viewMatrix.transform(movement);

			// Store movement vector in the client message bound for the server
//			ClientMessageHelper.addMovement(m_message, movement);

			// Retrieve the translation component from the transform
			Vector3f v = new Vector3f();
			v.set(avatar.getPosition());
			
			// Add the movement vector
			v.add(movement);

			// Update and set the new transformation
			avatar.setPosition(v);
			avatar.updateState();
			
			avatar.setLocomotionState(PlayerEntity.LOC_RUNNING);
		} else {
			avatar.setLocomotionState(PlayerEntity.LOC_IDLE);
		}

		// TODO Testing purposes only!!!!
		if (m_bFirePrimary) {
			System.err.println("avatar: " + m_avatar.getPosition() + " - " + m_avatar.getOrientation());
//			EntityBuilder.createBulletShape(m_client.getUniverse(), m_client.getAvatar().getPosition(), m_client.getAvatar().getOrientation(), 20.0f, 5.0f);
			Vector3f impulse = m_avatar.getOrientation();
			Point3f p = new Point3f(m_avatar.getPosition());
			p.y += 1.0f;
			EntityBuilder.createBullet(m_universe, p, impulse, 20.0f, 5.0f);
		}
		
		if ((_fCurrentAge - m_fLastUpdate) > 0.02f) {
			// Enough time has passed, let's send an update to the server
			ClientMessageHelper.addOrientation(m_message, avatar.getOrientation());
			ClientMessageHelper.addStateFlags(m_message, m_bFirePrimary);
			m_serverPort.sendPacket(m_message);
			m_serverPort.initPacket(m_message);
			m_fLastUpdate = _fCurrentAge;
		}
	}
	
	private void prepareTexture(RenderContext _context, int _nTextureId, String _sFileName) {
		try {
			Texture texture = TextureReader.readTexture(_context, _sFileName);
			_context.addTexture(_nTextureId, texture);
		} catch (IOException e) {
			System.err.println("Could not read texture because " + e.getMessage());
		}
	}

	class MenuWindow extends Window {
		public MenuWindow() {
			super("Test Window");
			setCenterParent(true);
			setWidth(300);
			setHeight(150);
			
			Text t = new Text("Welcome to the Batoru in-game menu pop-up window");
			t.setBounds(5, 5, 290, 40);
			add(t);
			Button b = new Button("Resume");
			b.setBounds(5, 45, 290, 20);
			b.addActionListener(new GuiActionListener() {
				public void actionPerformed(GuiActionEvent _event) {
					m_menuWindow.setVisible(false);
				}
			});
			add(b);
			b = new Button("Options");
			b.setBounds(5, 65, 290, 20);
			add(b);
			b = new Button("Exit this program");
			b.setBounds(5, 85, 290, 20);
			b.addActionListener(new GuiActionListener() {
				public void actionPerformed(GuiActionEvent _event) {
					m_view.stop();
				}
			});
			add(b);
			addKeyListener(new GuiKeyAdapter() {
				@Override
				public void keyPressed(GuiKeyEvent _event) {
					switch (_event.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						m_menuWindow.setVisible(false);
						break;
					}
					_event.consume();
				}
			});
		}
	}

	class InfoWindow extends Window {
		NumberFormat m_nf;
		Text m_fps, m_objectCount, m_liveCount, m_mortalCount;
		Text m_position, m_orientation, m_impulse;
		
		public InfoWindow() {
			m_nf = NumberFormat.getNumberInstance();
			m_nf.setMinimumFractionDigits(1);
			m_nf.setMaximumFractionDigits(1);

			setBounds(10, -140, 180, 130);
			setFocusable(false);
			
			Text t = new Text("FPS");
			t.setBounds(5, 5, 75, 20);
			add(t);
			m_fps = new Text("?");
			m_fps.setBounds(80, 5, 50, 20);
			add(m_fps);

			t = new Text("#objects");
			t.setBounds(5, 25, 75, 20);
			add(t);
			m_objectCount = new Text("?");
			m_objectCount.setBounds(80, 25, 50, 20);
			add(m_objectCount);

			t = new Text("#live");
			t.setBounds(5, 40, 75, 20);
			add(t);
			m_liveCount = new Text("?");
			m_liveCount.setBounds(80, 40, 50, 20);
			add(m_liveCount);

			t = new Text("#mortal");
			t.setBounds(5, 55, 75, 20);
			add(t);
			m_mortalCount = new Text("?");
			m_mortalCount.setBounds(80, 55, 50, 20);
			add(m_mortalCount);

			t = new Text("pos");
			t.setBounds(5, 75, 75, 20);
			add(t);
			m_position = new Text("?");
			m_position.setBounds(80, 75, 100, 20);
			add(m_position);

			t = new Text("look");
			t.setBounds(5, 90, 75, 20);
			add(t);
			m_orientation = new Text("?");
			m_orientation.setBounds(80, 90, 100, 20);
			add(m_orientation);

			t = new Text("impulse");
			t.setBounds(5, 105, 75, 20);
			add(t);
			m_impulse = new Text("?");
			m_impulse.setBounds(80, 105, 100, 20);
			add(m_impulse);
		}
		
		public void setFps(float _fFps) {
			Float f = new Float(_fFps);
			m_fps.setText(m_nf.format(f));
		}
		
		public void setObjectCount(String _sCount) {
			m_objectCount.setText(_sCount);
		}
		
		public void setLiveCount(String _sCount) {
			m_liveCount.setText(_sCount);
		}
		
		public void setMortalCount(String _sCount) {
			m_mortalCount.setText(_sCount);
		}
		
		public void setPosition(Point3f _position) {
			String sPos = m_nf.format(_position.x) + "," + m_nf.format(_position.y) + "," + m_nf.format(_position.z);
			m_position.setText(sPos);
		}
		
		public void setOrientation(Vector3f _orientation) {
			String sLook = m_nf.format(_orientation.x) + "," + m_nf.format(_orientation.y) + "," + m_nf.format(_orientation.z);
			m_orientation.setText(sLook);
		}
		
		public void setImpulse(Vector3f _impulse) {
			String sImpulse = m_nf.format(_impulse.x) + "," + m_nf.format(_impulse.y) + "," + m_nf.format(_impulse.z);
			m_impulse.setText(sImpulse);
		}
	}
	
	public void updateInfo(float _fFps) {
		m_infoWindow.setFps(_fFps);
		if (m_universe != null) {
			m_infoWindow.setObjectCount(String.valueOf(m_universe.getRenderablesList().size()));
			m_infoWindow.setLiveCount(String.valueOf(m_universe.getLiveEntitiesList().size()));
			m_infoWindow.setMortalCount(String.valueOf(m_universe.getTerminalEntitiesList().size()));
			if (m_avatar != null) {
				m_infoWindow.setPosition(m_avatar.getPosition());
				m_infoWindow.setOrientation(m_avatar.getOrientation());
				m_infoWindow.setImpulse(m_avatar.getImpulse());
			}
		}
	}
	
	private void grabMouse() {
		// TODO is this still necessary?
		m_bGrabMouse = true;
	}
	
	private void releaseMouse() {
		// TODO is this still necessary?
		m_bGrabMouse = false;
	}
	
	public void mouseClicked(GuiMouseEvent arg0) {
		grabMouse();
	}

	public void mousePressed(GuiMouseEvent arg0) {
		if (m_bGrabMouse) {
			m_bFirePrimary = true;
		}
	}

	public void mouseReleased(GuiMouseEvent arg0) {
		if (m_bGrabMouse) {
			m_bFirePrimary = false;
		}
	}

	public void mouseDragged(GuiMouseEvent _event) {
		handleMouseMove(_event);
	}

	public void mouseMoved(GuiMouseEvent _event) {
		handleMouseMove(_event);
	}
	
	private void handleMouseMove(GuiMouseEvent _event) {
		if (m_bGrabMouse) {
			double motionDelay = 0.5;
			
			int nCenterX = m_glWindow.getWidth() / 2;
			int nCenterY = m_glWindow.getHeight() / 2;
			
			m_fRotY -= (_event.getX() - nCenterX + 4) * motionDelay;
			m_fRotX -= (_event.getY() - nCenterY + 30) * motionDelay;

			Entity avatar = m_avatar;
			if (avatar != null) {
				// Retrieve the current orientation vector from the avatar
				Vector3f orientation = avatar.getOrientation();
				// Rotate a unit vector pointing towards negative Z
				Vectors.rotateVector(Vectors.VECTF_IN, m_fRotX, m_fRotY, 0, orientation);
				// Tell the entity we changed some of its parameters
				avatar.updateState();
			}
			
			// Center the mouse pointer
			m_aRobot.mouseMove(m_glWindow.getX() + nCenterX, m_glWindow.getY() + nCenterY);
		}
	}
	
	public void keyPressed(GuiKeyEvent _event) {
		handleKey(_event);
	}
	
	public void keyReleased(GuiKeyEvent _event) {
		handleKey(_event);
	}
	
	public void keyTyped(GuiKeyEvent _event) {
		// Not needed
	}
	
	private void handleKey(GuiKeyEvent _event) {
		boolean bDown = (_event.getId() == KeyEvent.KEY_PRESSED) || (_event.getId() == KeyEvent.KEY_TYPED);
		switch (_event.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			m_bKeyLeft = bDown;
			break;
		case KeyEvent.VK_RIGHT:
			m_bKeyRight = bDown;
			break;
		case KeyEvent.VK_UP:
			m_bKeyForward = bDown;
			break;
		case KeyEvent.VK_DOWN:
			m_bKeyBackward = bDown;
			break;
		case KeyEvent.VK_F10:
			releaseMouse();
			break;
		case KeyEvent.VK_ESCAPE:
			if (bDown) {
				m_menuWindow.setVisible(true);
				m_menuWindow.activate();
			}
			break;
		}
		switch (_event.getKeyChar()) {
		case 'a':
			m_bKeyLeft = bDown;
			break;
		case 'd':
			m_bKeyRight = bDown;
			break;
		case 'w':
			m_bKeyForward = bDown;
			break;
		case 's':
			m_bKeyBackward = bDown;
			break;
		case ' ':
			m_bKeyJump = bDown;
			break;
//			case 'o':
//				m_avatar.setImpulse(Vectors.VECTF_ZERO);
//				break;
		}
	}
}

/*
 * $Log$
 * Revision 1.6  2003/12/01 22:52:10  tako
 * Now supports mouse-handling in the GUI.
 * GUI getes updated correctly when resizing the window.
 * Added some info about the avatar to the info window.
 *
 * Revision 1.5  2003/11/18 11:06:28  tako
 * All times and ages are now floats, no longs anymore.
 * Removed some unused code.
 *
 * Revision 1.4  2003/11/17 13:18:50  tako
 * Changed the setFps() method of the InfoWindow to accept floats
 * instead of Strings.
 * Changed call to setKeyListener() to addKeyListener() because of API
 * change in the gui4gl package.
 * Re-enabled the old FPS display code to compare measure the impact of
 * the gui4gl windows in the frame rate.
 *
 */
