package games.batoru;
/*
 * Lesson06.java
 * 
 * Created on July 16, 2003, 11:30 AM
 */

import net.java.games.jogl.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import org.codejive.utils4gl.*;

/**
 * Port of the NeHe OpenGL Tutorial (Lesson 6) to Java using the Jogl interface
 * to OpenGL. Jogl can be obtained at http://jogl.dev.java.net/
 * 
 * @author Kevin Duling (jattier@hotmail.com)
 */
class TextureTest implements GLEventListener {
	private float xrot; // X Rotation ( NEW )
	private float yrot; // Y Rotation ( NEW )
	private float zrot; // Z Rotation ( NEW )

	private int[] textures;

	public static void main(String[] args) {
		GLDisplay neheGLDisplay = GLDisplay.createGLDisplay("Lesson 06: Texture mapping");
		neheGLDisplay.addGLEventListener(new TextureTest());
		neheGLDisplay.start();
	}

	/**
	 * Called by the drawable to initiate OpenGL rendering by the client. After
	 * all GLEventListeners have been notified of a display event, the drawable
	 * will swap its buffers if necessary.
	 * 
	 * @param gLDrawable
	 *            The GLDrawable object.
	 */
	public void display(GLDrawable gLDrawable) {
		final GL gl = gLDrawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity(); // Reset The View
		gl.glTranslatef(0.0f, 0.0f, -5.0f);

		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);

		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);

		gl.glBegin(GL.GL_QUADS);
		// Front Face
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		// Back Face
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		// Top Face
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		// Bottom Face
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		// Right face
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, -1.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		// Left Face
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		gl.glEnd();

		xrot += 0.3f;
		yrot += 0.2f;
		zrot += 0.4f;
	}

	/**
	 * Called when the display mode has been changed. <B>!! CURRENTLY
	 * UNIMPLEMENTED IN JOGL !!</B>
	 * 
	 * @param gLDrawable
	 *            The GLDrawable object.
	 * @param modeChanged
	 *            Indicates if the video mode has changed.
	 * @param deviceChanged
	 *            Indicates if the video device has changed.
	 */
	public void displayChanged(GLDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
		// Not used
	}

	/**
	 * Called by the drawable immediately after the OpenGL context is
	 * initialized for the first time. Can be used to perform one-time OpenGL
	 * initialization such as setup of lights and display lists.
	 * 
	 * @param gLDrawable
	 *            The GLDrawable object.
	 */
	public void init(GLDrawable gLDrawable) {
		final GL gl = gLDrawable.getGL();

		//	  gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, pos);
		gl.glEnable(GL.GL_CULL_FACE);
		//	  gl.glEnable(GL.GL_LIGHTING);
		//	  gl.glEnable(GL.GL_LIGHT0);
		//	  gl.glShadeModel(GL.GL_SMOOTH); // Enable Smooth Shading
		//	  gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
		//	  gl.glClearDepth(1.0f); // Depth Buffer Setup
		gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
		//	  gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Really
																	// Nice
																	// Perspective
																	// Calculations
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glEnable(GL.GL_NORMALIZE);

		//      gl.glShadeModel(GL.GL_SMOOTH); // Enable Smooth Shading
		//      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
		//      gl.glClearDepth(1.0f); // Depth Buffer Setup
		//      gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
		//      gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
		//      gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); //
		// Really Nice Perspective Calculations
		//      gl.glEnable(GL.GL_TEXTURE_2D);

		textures = new int[1];
		gl.glGenTextures(1, textures);

		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);
		Texture texture = null;
		try {
			texture = TextureReader.readTexture("demos/data/textures/boden.bmp");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		gl.glTexImage2D(
			GL.GL_TEXTURE_2D,
			0,
			GL.GL_RGB,
			texture.getWidth(),
			texture.getHeight(),
			0,
			GL.GL_RGB,
			GL.GL_UNSIGNED_BYTE,
			texture.getPixels());
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	}

	/**
	 * Called by the drawable during the first repaint after the component has
	 * been resized. The client can update the viewport and view volume of the
	 * window appropriately, for example by a call to GL.glViewport(int, int,
	 * int, int); note that for convenience the component has already called
	 * GL.glViewport(int, int, int, int)(x, y, width, height) when this method
	 * is called, so the client may not have to do anything in this method.
	 * 
	 * @param gLDrawable
	 *            The GLDrawable object.
	 * @param x
	 *            The X Coordinate of the viewport rectangle.
	 * @param y
	 *            The Y coordinate of the viewport rectanble.
	 * @param width
	 *            The new width of the window.
	 * @param height
	 *            The new height of the window.
	 */
	public void reshape(GLDrawable gLDrawable, int x, int y, int width, int height) {
		final GL gl = gLDrawable.getGL();
		final GLU glu = gLDrawable.getGLU();

		if (height <= 0) // avoid a divide by zero error!
			height = 1;
		final float h = (float)width / (float)height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, h, 1.0, 20.0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}

class GLDisplay {
	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;

	private static final int DONT_CARE = -1;

	private JFrame frame;
	private GLCanvas glCanvas;
	private Animator animator;
	private boolean fullscreen;
	private int width;
	private int height;
	private GraphicsDevice usedDevice;

	public static GLDisplay createGLDisplay(String title) {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		boolean fullscreen = false;
		if (device.isFullScreenSupported()) {
			int selectedOption =
				JOptionPane.showOptionDialog(
					null,
					"How would you like to run this lesson?",
					title,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					new Object[] { "Fullscreen", "Windowed" },
					"Windowed");
			fullscreen = selectedOption == 0;
		}
		return new GLDisplay(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, fullscreen);
	}

	public GLDisplay(String title, boolean fullscreen) {
		this(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, fullscreen);
	}

	public GLDisplay(String title, int width, int height, boolean fullscreen) {
		glCanvas = GLDrawableFactory.getFactory().createGLCanvas(new GLCapabilities());
		glCanvas.setSize(width, height);
		glCanvas.setIgnoreRepaint(true);

		frame = new JFrame(title);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(glCanvas, BorderLayout.CENTER);

		addKeyListener(new MyShutdownKeyAdapter());

		this.fullscreen = fullscreen;
		this.width = width;
		this.height = height;
		animator = new Animator(glCanvas);
	}

	public void start() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(width, height);
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.addWindowListener(new MyShutdownWindowAdapter());

		if (fullscreen) {
			frame.setUndecorated(true);
			usedDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			usedDevice.setFullScreenWindow(frame);
			usedDevice.setDisplayMode(
				findDisplayMode(
					usedDevice.getDisplayModes(),
					width,
					height,
					usedDevice.getDisplayMode().getBitDepth(),
					usedDevice.getDisplayMode().getRefreshRate()));
		} else {
			frame.setVisible(true);
		}

		glCanvas.requestFocus();

		animator.start();
	}

	public void stop() {
		animator.stop();
		if (fullscreen) {
			usedDevice.setFullScreenWindow(null);
			usedDevice = null;
		}
		frame.dispose();
		System.exit(0);
	}

	private DisplayMode findDisplayMode(
		DisplayMode[] displayModes,
		int requestedWidth,
		int requestedHeight,
		int requestedDepth,
		int requestedRefreshRate) {
		// Try to find an exact match
		DisplayMode displayMode =
			findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, requestedDepth, requestedRefreshRate);

		// Try again, ignoring the requested bit depth
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, DONT_CARE, DONT_CARE);

		// Try again, and again ignoring the requested bit depth and height
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, requestedWidth, DONT_CARE, DONT_CARE, DONT_CARE);

		// If all else fails try to get any display mode
		if (displayMode == null)
			displayMode = findDisplayModeInternal(displayModes, DONT_CARE, DONT_CARE, DONT_CARE, DONT_CARE);

		return displayMode;
	}

	private DisplayMode findDisplayModeInternal(
		DisplayMode[] displayModes,
		int requestedWidth,
		int requestedHeight,
		int requestedDepth,
		int requestedRefreshRate) {
		DisplayMode displayModeToUse = null;
		for (int i = 0; i < displayModes.length; i++) {
			DisplayMode displayMode = displayModes[i];
			if ((requestedWidth == DONT_CARE || displayMode.getWidth() == requestedWidth)
				&& (requestedHeight == DONT_CARE || displayMode.getHeight() == requestedHeight)
				&& (requestedHeight == DONT_CARE || displayMode.getRefreshRate() == requestedRefreshRate)
				&& (requestedDepth == DONT_CARE || displayMode.getBitDepth() == requestedDepth))
				displayModeToUse = displayMode;
		}

		return displayModeToUse;
	}

	public void addGLEventListener(GLEventListener glEventListener) {
		glCanvas.addGLEventListener(glEventListener);
	}

	public void removeGLEventListener(GLEventListener glEventListener) {
		glCanvas.removeGLEventListener(glEventListener);
	}

	public void addKeyListener(KeyListener l) {
		glCanvas.addKeyListener(l);
	}

	public void addMouseListener(MouseListener l) {
		glCanvas.addMouseListener(l);
	}

	public void addMouseMotionListener(MouseMotionListener l) {
		glCanvas.addMouseMotionListener(l);
	}

	public void removeKeyListener(KeyListener l) {
		glCanvas.removeKeyListener(l);
	}

	public void removeMouseListener(MouseListener l) {
		glCanvas.removeMouseListener(l);
	}

	public void removeMouseMotionListener(MouseMotionListener l) {
		glCanvas.removeMouseMotionListener(l);
	}

	private class MyShutdownKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				stop();
			}
		}
	}

	private class MyShutdownWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			stop();
		}
	}
}
