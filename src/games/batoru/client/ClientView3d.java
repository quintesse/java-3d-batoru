/*
 * Created on Oct 1, 2003
 */
package games.batoru.client;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.vecmath.*;

import net.java.games.jogl.*;
import net.java.games.jogl.util.GLUT;

import org.codejive.world3d.*;
import org.codejive.gui4gl.events.GuiActionEvent;
import org.codejive.gui4gl.events.GuiActionListener;
import org.codejive.gui4gl.events.GuiKeyAdapter;
import org.codejive.gui4gl.events.GuiKeyEvent;
import org.codejive.gui4gl.events.GuiKeyListener;
import org.codejive.gui4gl.events.GuiMouseEvent;
import org.codejive.gui4gl.events.GuiMouseListener;
import org.codejive.gui4gl.widgets.*;
import org.codejive.world3d.net.*;
import org.codejive.utils4gl.*;

import games.batoru.EntityBuilder;
import games.batoru.entities.PlayerEntity;
import games.batoru.net.ClientMessageHelper;

/**
 * @author Tako
 * @version $Revision: 175 $
 */
public class ClientView3d implements NetworkDecoder, GuiMouseListener, GuiKeyListener {
	private String m_sTitle;
	private int m_nWidth, m_nHeight;
	private boolean m_bFullscreen;

	private JFrame m_clientFrame;
	private Screen m_screen;
	protected MenuWindow m_menuWindow;
	protected InfoWindow m_infoWindow;
	private GraphicsDevice m_device;
	private Animator m_animator;
	
	private Robot m_aRobot;
	private boolean m_bGrabMouse;
	
	private float m_fRotX = 0.0f;
	private float m_fRotY = 0.0f;

	private boolean m_bKeyLeft = false;
	private boolean m_bKeyRight = false;
	private boolean m_bKeyForward = false;
	private boolean m_bKeyBackward = false;	private boolean m_bKeyJump = false;
	private boolean m_bFirePrimary = false;

	private float m_fSurfaceSpeed = 180.0f;	// meters per second
	private float m_fAirSpeed = 90.0f;	// meters per second

	private final Vector3f VECTF_JUMP = new Vector3f(0.0f, 15.0f, 0.0f);
	
	private float m_fLastAge = 0;
	private float m_fLastUpdate = 0;

	protected Client m_client;
	
	private MessagePort m_serverPort;
	private MessagePacket m_message;
	
	// These are only here to supposedly speed things up a bit	
	private Matrix3f m_viewMatrix = new Matrix3f();
	private Matrix3f m_tempMatrix = new Matrix3f();

	private static final int DEFAULT_WIDTH = 1024;
	private static final int DEFAULT_HEIGHT = 768;

	public ClientView3d(Client _client, String _sTitle, boolean _bFullscreen) {
		this(_client, _sTitle, DEFAULT_WIDTH, DEFAULT_HEIGHT, _bFullscreen);
	}
	
	public ClientView3d(Client _client, String _sTitle, int _nWidth, int _nHeight, boolean _bFullscreen) {
		m_sTitle = _sTitle;
		m_nWidth = _nWidth;
		m_nHeight = _nHeight;
		m_bFullscreen = _bFullscreen;
		m_client = _client;
	}

	protected void start() {
		GearRenderer renderer = new GearRenderer(this, m_client.getUniverse(), m_client.getAvatar());

		GLCanvas canvas = GLDrawableFactory.getFactory().createGLCanvas(new GLCapabilities());
		canvas.setSize(m_nWidth, m_nHeight);
		canvas.setIgnoreRepaint(true);

		String sTitle = m_sTitle;
		if (!m_bFullscreen) {
			sTitle += " (Press <F10> to release mouse)";
		}
		m_clientFrame = new JFrame(sTitle);
		m_clientFrame.getContentPane().setLayout(new BorderLayout());
		m_clientFrame.getContentPane().add(canvas, BorderLayout.CENTER);

		canvas.addGLEventListener(renderer);
//		canvas.addMouseListener(this);
//		canvas.addMouseMotionListener(this);
//		canvas.addKeyListener(this);
		m_clientFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
				m_client.stop();
			}
		});

		m_screen = new Screen();
		m_menuWindow = new MenuWindow();
		m_screen.add(m_menuWindow);
		m_infoWindow = new InfoWindow();
		m_infoWindow.setVisible(true);
		m_screen.add(m_infoWindow);
		canvas.addKeyListener(m_screen);
		canvas.addMouseListener(m_screen);
		canvas.addMouseMotionListener(m_screen);
		m_screen.addKeyListener(this);
		m_screen.addMouseListener(this);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		m_clientFrame.setSize(m_nWidth, m_nHeight);
		m_clientFrame.setLocation(
			(screenSize.width - m_clientFrame.getWidth()) / 2,
			(screenSize.height - m_clientFrame.getHeight()) / 2
		);

		if (m_bFullscreen) {
			m_clientFrame.setUndecorated(true);
			m_device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			m_device.setFullScreenWindow(m_clientFrame);
			m_device.setDisplayMode(
				findDisplayMode(
					m_device.getDisplayModes(),
					m_nWidth, m_nHeight,
					m_device.getDisplayMode().getBitDepth(),
					m_device.getDisplayMode().getRefreshRate()
				)
			);
		} else {
			m_clientFrame.setVisible(true);
		}

		canvas.requestFocus();
		
		try {
			m_aRobot = new Robot();
			// Center the mouse pointer
			m_aRobot.mouseMove(m_clientFrame.getX() + m_clientFrame.getWidth() / 2, m_clientFrame.getY() + m_clientFrame.getHeight() / 2);
			grabMouse();
		} catch (AWTException e) {
			System.err.println(e);
		}
		
		// Use debug pipeline
		//    canvas.setGL(new DebugGL(canvas.getGL()));
		System.err.println("CANVAS GL IS: " + canvas.getGL().getClass().getName());
		System.err.println("CANVAS GLU IS: " + canvas.getGLU().getClass().getName());

		m_serverPort = m_client.getMessagePort();
		m_message = new MessagePacket();
		m_serverPort.initPacket(m_message);
		
		m_animator = new Animator(canvas);
		m_animator.start();
	}
	
	protected void stop() {
		if (m_animator != null) {
			m_animator.stop();
			m_animator = null;
		}
		if (m_device != null) {
			m_device.setFullScreenWindow(null);
			m_device = null;
		}
		if (m_clientFrame != null) {
			m_clientFrame.dispose();
			m_clientFrame = null;
		}
	}
	
	private void grabMouse() {
		// Center the mouse pointer
		m_aRobot.mouseMove(m_clientFrame.getX() + m_clientFrame.getWidth() / 2, m_clientFrame.getY() + m_clientFrame.getHeight() / 2);
		m_clientFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().createImage(""), new Point(0, 0), "empty"));
		m_bGrabMouse = true;
	}
	
	private void releaseMouse() {
		// Center the mouse pointer
		m_clientFrame.setCursor(Cursor.DEFAULT_CURSOR);
		m_bGrabMouse = false;
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

	private DisplayMode findDisplayMode(DisplayMode[] displayModes, int requestedWidth, int requestedHeight, int requestedDepth, int requestedRefreshRate) {
		// Try to find an exact match
		DisplayMode displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, requestedDepth, requestedRefreshRate);

		// Try again, ignoring the requested bit depth
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, -1, -1);

		// Try again, and again ignoring the requested bit depth and height
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, -1, -1, -1);

		// If all else fails try to get any display mode
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, -1, -1, -1, -1);

		return displayMode;
	}

	private DisplayMode findDisplayModeInternal(DisplayMode[] displayModes, int requestedWidth, int requestedHeight, int requestedDepth, int requestedRefreshRate) {
		DisplayMode displayModeToUse = null;
		for (int i = 0; i < displayModes.length; i++) {
			DisplayMode displayMode = displayModes[i];
			if ((requestedWidth == -1 || displayMode.getWidth() == requestedWidth) &&
					(requestedHeight == -1 || displayMode.getHeight() == requestedHeight) &&
					(requestedHeight == -1 || displayMode.getRefreshRate() == requestedRefreshRate) &&
					(requestedDepth == -1 || displayMode.getBitDepth() == requestedDepth))
				displayModeToUse = displayMode;
		}

		return displayModeToUse;
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
	
			int nCenterX = m_clientFrame.getWidth() / 2;
			int nCenterY = m_clientFrame.getHeight() / 2;
			
			m_fRotY -= (_event.getX() - nCenterX + 4) * motionDelay;
			m_fRotX -= (_event.getY() - nCenterY + 30) * motionDelay;

			Entity avatar = m_client.getAvatar();

			// Retrieve the current orientation vector from the avatar
			Vector3f orientation = avatar.getOrientation();
			// Rotate a unit vector pointing towards negative Z
			Vectors.rotateVector(Vectors.VECTF_IN, m_fRotX, m_fRotY, 0, orientation);
			// Tell the entity we changed some of its parameters
			avatar.updateState();
	
			// Center the mouse pointer
			m_aRobot.mouseMove(m_clientFrame.getX() + nCenterX, m_clientFrame.getY() + nCenterY);
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

	protected void handleUniverseFrame() {
		float fCurrentAge = m_client.getUniverse().getAge();
		if (m_fLastAge > 0) {
			float fElapsedTime = fCurrentAge - m_fLastAge;
			m_client.getUniverse().handleFrame(fElapsedTime);
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

		PlayerEntity avatar = (PlayerEntity)m_client.getAvatar();

		// Check if the player wants to jump and if it is possible
		float fGravFactor = avatar.getGravityFactor();
		if (m_bKeyJump && (Math.abs(fGravFactor) > Universe.ALMOST_ZERO) && ((PlayerEntity)avatar).isOnSurface()) {
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
			float fSpeed = (((PlayerEntity)avatar).isOnSurface()) ? m_fSurfaceSpeed : m_fAirSpeed;
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
System.err.println("avatar: " + m_client.getAvatar().getPosition() + " - " + m_client.getAvatar().getOrientation());
//			EntityBuilder.createBulletShape(m_client.getUniverse(), m_client.getAvatar().getPosition(), m_client.getAvatar().getOrientation(), 20.0f, 5.0f);
			Vector3f impulse = m_client.getAvatar().getOrientation();
			Point3f p = new Point3f(m_client.getAvatar().getPosition());
			p.y += 1.0f;
			EntityBuilder.createBullet(m_client.getUniverse(), p, impulse, 20.0f, 5.0f);
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
	
	public void updateInfo(float _fFps, PhysicalEntity _avatar) {
		m_infoWindow.setFps(_fFps);
		m_infoWindow.setObjectCount(String.valueOf(m_client.getUniverse().getRenderablesList().size()));
		m_infoWindow.setLiveCount(String.valueOf(m_client.getUniverse().getLiveEntitiesList().size()));
		m_infoWindow.setMortalCount(String.valueOf(m_client.getUniverse().getTerminalEntitiesList().size()));
		m_infoWindow.setPosition(_avatar.getPosition());
		m_infoWindow.setOrientation(_avatar.getOrientation());
		m_infoWindow.setImpulse(_avatar.getImpulse());
	}

	public Screen getGUI() {
		return m_screen;
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
					stop();
					m_client.stop();
				}
			});
			add(b);
			addKeyListener(new GuiKeyAdapter() {
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
}

class GearRenderer implements GLEventListener {
	private ClientView3d m_view;
	private Universe m_universe;
	private Entity m_avatar;
	private UniverseRenderer m_universeRenderer;

	private RenderContext m_context;
	private FrameRateCounter m_frameRateCounter;
	
	public GearRenderer(ClientView3d _view, Universe _universe, Entity _avatar) {
		m_view = _view;
		m_universe = _universe;
		m_avatar = _avatar;
		m_universeRenderer = null;
	}
		
	public void init(GLDrawable drawable) {
		GL gl = drawable.getGL();
		GLU glu = drawable.getGLU();

		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.glEnable(GL.GL_CULL_FACE);
		gl.glShadeModel(GL.GL_SMOOTH);              // Enable Smooth Shading
//		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
//		gl.glClearDepth(1.0f);                      // Depth Buffer Setup
		gl.glEnable(GL.GL_DEPTH_TEST);				// Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL);				// The Type Of Depth Testing To Do
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);	// Really Nice Perspective Calculations
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glEnable(GL.GL_NORMALIZE);
			
		// Set up lighting
		float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
		float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition);
		gl.glEnable(GL.GL_LIGHT1);
//		gl.glEnable(GL.GL_LIGHTING);

		m_context = new RenderContext(gl, glu);
		m_context.setTexture(0, "games/batoru/textures/grass_03.jpg");

//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_NEAREST);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_NEAREST);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

		m_universeRenderer = new UniverseRenderer(m_universe, m_avatar);
		m_universeRenderer.initRendering(m_context);

		m_frameRateCounter = new SimpleFrameRateCounter();
		
		m_view.getGUI().initRendering(m_context);
		
		// Lower the priority of the render thread so we don't affect the other threads (much)
		Thread.currentThread().setPriority(Thread.currentThread().getPriority() - 1);
	}

	public void reshape(GLDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		GLU glu = drawable.getGLU();

		float h = (float) width / (float) height;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);

		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		System.err.println();
		System.err.println("glLoadTransposeMatrixfARB() supported: " + gl.isFunctionAvailable("glLoadTransposeMatrixfARB"));
		if (!gl.isFunctionAvailable("glLoadTransposeMatrixfARB")) {
			// --- not using extensions
			gl.glLoadIdentity();
		} else {
			// --- using extensions
			final float[] identityTranspose = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
			gl.glLoadTransposeMatrixfARB(identityTranspose);
		}
		glu.gluPerspective(45.0f, h, 1.0, 600.0);
//		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 600.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);

		m_view.getGUI().updateRendering(m_context);
	}

	public void display(GLDrawable drawable) {
		GL gl = drawable.getGL();

		m_frameRateCounter.addFrame();

		m_view.handleUniverseFrame();
			
		gl.glLoadIdentity();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glPushMatrix();
			
		m_universeRenderer.render(m_context);
			
		gl.glPopMatrix();

		renderFrameRate(m_context, m_frameRateCounter.getFrameRate());
		m_view.updateInfo(m_frameRateCounter.getFrameRate(), m_avatar);
		
		m_view.getGUI().render(m_context);
	}

	public void displayChanged(GLDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// Not needed
	}


	private void renderFrameRate(RenderContext _context, float _fFps) {
		GL gl = _context.getGl();

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(1);
		Float f = new Float(_fFps);

		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix ();
		gl.glLoadIdentity();
 
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);
		_context.getGlu().gluOrtho2D(0, viewport[2], viewport[3], 0);
		gl.glDepthFunc(GL.GL_ALWAYS);

		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(15, 15);
		_context.getGlut().glutBitmapString(gl, GLUT.BITMAP_HELVETICA_18, "FPS: " + nf.format(f));
 
		gl.glDepthFunc(GL.GL_LESS);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix (); 		
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
