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
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.vecmath.*;

import net.java.games.jogl.*;
import net.java.games.jogl.util.*;

import org.codejive.world3d.*;
import org.codejive.gui4gl.widgets.*;
import org.codejive.world3d.net.*;
import org.codejive.utils4gl.*;

import games.batoru.EntityBuilder;
import games.batoru.entities.Player;
import games.batoru.net.ClientMessageHelper;
import games.batoru.shapes.PlayerShape;

/**
 * @author Tako
 */
public class ClientView3d implements NetworkDecoder, MouseListener, MouseMotionListener, KeyListener {
	private String m_sTitle;
	private int m_nWidth, m_nHeight;
	private boolean m_bFullscreen;

	private JFrame m_clientFrame;
	private Screen m_screen;
	private Window m_menuWindow;
	private Window m_infoWindow;
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
	
	private long m_lLastSystemTime = 0;
	private long m_lLastUpdateTime = 0;

	private Client m_client;
	
	private MessagePort m_serverPort;
	private MessagePacket m_message;
	private int m_nEmptyMessageSize;
	
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
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
//		canvas.addKeyListener(this);
		m_clientFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
				m_client.stop();
			}
		});

		m_screen = new Screen();
		m_menuWindow = new Window("Test Window");
		m_menuWindow.setCenterParent(true);
		m_menuWindow.setWidth(300);
		m_menuWindow.setHeight(150);
		Text t = new Text("Welcome to the Batoru in-game menu pop-up window");
		t.setBounds(5, 5, 290, 40);
		m_menuWindow.add(t);
		Button b = new Button("Resume");
		b.setBounds(5, 45, 290, 20);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _event) {
				m_menuWindow.setVisible(false);
			}
		});
		m_menuWindow.add(b);
		b = new Button("Options");
		b.setBounds(5, 65, 290, 20);
		m_menuWindow.add(b);
		b = new Button("Exit this program");
		b.setBounds(5, 85, 290, 20);
/*
	b.setCaptionAlignment(TextAlignment.ALIGN_CENTER);
	// BEGIN TEST
	b.addKeyListener(new KeyAdapter() {
		public void keyTyped(KeyEvent _event) {
			Button b = (Button)_event.getSource();
			switch (_event.getKeyChar()) {
				case 'a':
					b.setWidth(b.getWidth() - 1);
					break;
				case 'd':
					b.setWidth(b.getWidth() + 1);
					break;
				case 'w':
					b.setHeight(b.getHeight() - 1);
					break;
				case 's':
					b.setHeight(b.getHeight() + 1);
					break;
			}
		}
	});
	// END TEST
 */
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _event) {
				stop();
				m_client.stop();
			}
		});
		m_menuWindow.add(b);
		m_menuWindow.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent _event) {
				switch (_event.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						m_menuWindow.setVisible(false);
						break;
				}
			}
		});
		m_screen.add(m_menuWindow);
//		canvas.addMouseListener(m_screen);
//		canvas.addMouseMotionListener(m_screen);
		canvas.addKeyListener(m_screen);
//		m_screen.addMouseListener(this);
//		m_screen.addMouseMotionListener(this);
		m_screen.setKeyListener(this);

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
		m_nEmptyMessageSize = m_message.getSize();
		
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

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		grabMouse();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		m_bFirePrimary = true;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		m_bFirePrimary = false;
	}

	public void mouseDragged(MouseEvent _event) {
		handleMouseMove(_event);
	}

	public void mouseMoved(MouseEvent _event) {
		handleMouseMove(_event);
	}
		
	private void handleMouseMove(MouseEvent _event) {
		if (m_bGrabMouse) {
			double motionDelay = 0.5;
	
			int nCenterX = m_clientFrame.getWidth() / 2;
			int nCenterY = m_clientFrame.getHeight() / 2;
			
			m_fRotY -= (_event.getX() - nCenterX + 4) * motionDelay;
			m_fRotX -= (_event.getY() - nCenterY + 30) * motionDelay;

			Shape avatar = m_client.getAvatar();

			// Retrieve the current orientation vector from the avatar
			Vector3f orientation = avatar.getOrientation();
			// Set its new values
			orientation.x = m_fRotX;
			orientation.y = m_fRotY;
			orientation.z = 0;
			// Tell the entity we changed some of its parameters
			avatar.updateState();
	
			// Center the mouse pointer
			m_aRobot.mouseMove(m_clientFrame.getX() + nCenterX, m_clientFrame.getY() + nCenterY);
		}
	}
	
	public void keyPressed(KeyEvent _event) {
		handleKey(_event);
	}
		
	public void keyReleased(KeyEvent _event) {
		handleKey(_event);
	}
		
	public void keyTyped(KeyEvent _event) {
	}
		
	private void handleKey(KeyEvent _event) {
		boolean bDown = (_event.getID() == KeyEvent.KEY_PRESSED) || (_event.getID() == KeyEvent.KEY_TYPED);
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
		long lCurrentSystemTime = m_client.getUniverse().getAge();
		if (m_lLastSystemTime > 0) {
			float fElapsedTime = (float)(lCurrentSystemTime - m_lLastSystemTime) / 1000;
			m_client.getUniverse().handleFrame(fElapsedTime);
			handleAvatarFrame(lCurrentSystemTime);
		}
		m_lLastSystemTime = lCurrentSystemTime;
	}
	
	protected void handleAvatarFrame(long _lCurrentSystemTime) {
		float fElapsedTime = (float)(_lCurrentSystemTime - m_lLastSystemTime) / 1000;

		int nXMovement = 0;
		int nZMovement = 0;

		// Depending on the left and right keys the X movement is -1, 1 or 0
		nXMovement += (m_bKeyLeft) ? -1 : 0;
		nXMovement += (m_bKeyRight) ? 1 : 0;
		// Depending on the forward and backward keys the Z movement is -1, 1 or 0
		nZMovement += (m_bKeyForward) ? -1 : 0;
		nZMovement += (m_bKeyBackward) ? 1 : 0;

		PlayerShape avatar = (PlayerShape)m_client.getAvatar();

		// Check if the player wants to jump and if it is possible
		float fGravFactor = avatar.getGravityFactor();
		if (m_bKeyJump && (Math.abs(fGravFactor) > Universe.ALMOST_ZERO) && ((PlayerShape)avatar).isOnSurface()) {
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
			float fSpeed = (((PlayerShape)avatar).isOnSurface()) ? m_fSurfaceSpeed : m_fAirSpeed;
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
			
			avatar.setLocomotionState(Player.LOC_RUNNING);
		} else {
			avatar.setLocomotionState(Player.LOC_IDLE);
		}

		// TODO Testing purposes only!!!!
		if (m_bFirePrimary) {
			Shape bullet = EntityBuilder.createBulletShape(m_client.getUniverse(), m_client.getAvatar().getPosition(), m_client.getAvatar().getOrientation(), 20.0f, 5.0f);
		}
		
		if ((_lCurrentSystemTime - m_lLastUpdateTime) > 50) {
			// Enough time has passed, let's send an update to the server
			ClientMessageHelper.addOrientation(m_message, avatar.getOrientation());
			ClientMessageHelper.addStateFlags(m_message, m_bFirePrimary);
			m_serverPort.sendPacket(m_message);
			m_serverPort.initPacket(m_message);
			m_nEmptyMessageSize = m_message.getSize();
			m_lLastUpdateTime = _lCurrentSystemTime;
		}
	}
	
	public Screen getGUI() {
		return m_screen;
	}
}


class GearRenderer implements GLEventListener {
	private ClientView3d m_view;
	private Universe m_universe;
	private Shape m_avatar;
	private UniverseRenderer m_universeRenderer;

	private RenderContext m_context;
	private FrameRateCounter m_frameRateCounter;
	
	public GearRenderer(ClientView3d _view, Universe _universe, Shape _avatar) {
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
		m_universeRenderer = new UniverseRenderer(m_context, m_universe, m_avatar);
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
		
		m_view.getGUI().render(m_context);
		
//		try { Thread.currentThread().sleep(0, 1000); } catch (Exception e) {}
	}

	public void displayChanged(GLDrawable drawable, boolean modeChanged, boolean deviceChanged) {
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