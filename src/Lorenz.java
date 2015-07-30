/**
 * Lorenz Attractor in OpenGL
 * 
 * @author John Burnett
 */


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.*;
import javax.media.opengl.awt.GLCanvas;


public class Lorenz implements GLEventListener, 
							   ActionListener, 
							   ChangeListener, 
							   KeyListener {
	
	public static void main(String[] args) { new Lorenz(); }
	
	private float t = 0.005f;
	private float initX = 0.1f;
	private float initY  = 0f;
	private float initZ = 0f;
	private float A = 10f;
	private float B = 28f;
	private float C = 8f / 3f;
	private int ITER = 50000;
	private float RADIUS = 0.25f;
	private float viewX = 5;
	private float viewY = 90;
	private float viewZ = 20;
	private float lastX = 0.1f;
	private float lastY = 0.1f;
	private float lastZ = 0.1f;
	private int TRAIL_LENGTH = 3000;
	private int TRAIL_SPEED = 450;
	private final int INTITIAL_WIDTH = 700;
	private final int INITIAL_HEIGHT = 700;
	private final int FPS = 30;
	private final float BG[] = {0.1f, 0.1f, 0.1f};
	
	private FPSAnimator animator;
	private boolean animate = false;
	
	private GLCanvas canvas;
	private GL2 gl;
	private GLU glu;
	JButton startButton, stopButton, quitButton, resetButton;
	private JSlider trailLengthSlider, trailSpeedSlider, aSlider, bSlider, cSlider;
	
	
	public  Lorenz() {
			GLProfile glp=GLProfile.getDefault();
			GLCapabilities caps = new GLCapabilities(glp);
			canvas = new GLCanvas(caps);
			canvas.addGLEventListener(this);
			canvas.addKeyListener(this);
			JFrame frame = new JFrame("Lorenz Attractor");

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
			frame.setSize(INTITIAL_WIDTH, INITIAL_HEIGHT);
			frame.setLayout(new BorderLayout());
			JPanel north = new JPanel( new BorderLayout());
			JPanel topRow = new JPanel();
			JPanel bottomRow = new JPanel( new GridLayout(2, 2) );
			JPanel bottomWest = new JPanel();
			JPanel bottomEast = new JPanel();
			
			startButton = new JButton("Start");
			startButton.addActionListener(this);
			topRow.add(startButton);
			topRow.add(new JLabel( "    "));
			stopButton = new JButton("Stop");
			stopButton.addActionListener(this);
			topRow.add(stopButton);
			topRow.add(new JLabel( "    "));
			resetButton = new JButton("Reset");
			resetButton.addActionListener(this);
			topRow.add(resetButton);
			topRow.add(new JLabel( "    "));
			quitButton = new JButton("Quit");
			quitButton.addActionListener(this);
			topRow.add(quitButton);
			
			trailLengthSlider = new JSlider(0, 5000);
			trailLengthSlider.setValue((int)(TRAIL_LENGTH));
			trailLengthSlider.setMajorTickSpacing(1000);
			trailLengthSlider.setPaintTicks(true);
			trailLengthSlider.setPaintLabels(true);
			trailLengthSlider.addChangeListener(this);
			JLabel lengthLabel = new JLabel("Length");
			bottomWest.add(lengthLabel);
			bottomWest.add(trailLengthSlider);
			
			trailSpeedSlider = new JSlider(0, 1000);
			trailSpeedSlider.setValue((int)(TRAIL_SPEED));
			trailSpeedSlider.setMajorTickSpacing(250);
			trailSpeedSlider.setPaintTicks(true);
			trailSpeedSlider.setPaintLabels(true);
			trailSpeedSlider.addChangeListener(this);
			JLabel speedLabel = new JLabel("Speed");
			bottomWest.add(speedLabel);
			bottomWest.add(trailSpeedSlider);
			
			aSlider = new JSlider(0, 50);
			aSlider.setValue((int)(A));
			aSlider.setMajorTickSpacing(10);
			aSlider.setPaintTicks(true);
			aSlider.setPaintLabels(true);
			aSlider.addChangeListener(this);
			JLabel aLabel = new JLabel("A");
			bottomEast.add(aLabel);
			bottomEast.add(aSlider);
			
			bSlider = new JSlider(5, 15);
			bSlider.setValue((int)(B));
			bSlider.setMajorTickSpacing(5);
			bSlider.setPaintTicks(true);
			bSlider.setPaintLabels(true);
			bSlider.addChangeListener(this);
			JLabel bLabel = new JLabel("B");
			bottomEast.add(bLabel);
			bottomEast.add(bSlider);
			
			cSlider = new JSlider(0, 10);
			cSlider.setValue((int)(C));
			cSlider.setMajorTickSpacing(5);
			cSlider.setPaintTicks(true);
			cSlider.setPaintLabels(true);
			cSlider.addChangeListener(this);
			JLabel cLabel = new JLabel("C");
			bottomEast.add(cLabel);
			bottomEast.add(cSlider);
			
			bottomRow.add(bottomWest);
			bottomRow.add(bottomEast);
			north.add(topRow, BorderLayout.NORTH);
			north.add(bottomRow, BorderLayout.SOUTH);
			frame.add(north, BorderLayout.NORTH);
			JPanel myCanvas = new JPanel(new GridLayout(1,1));
			
			myCanvas.add(canvas);

			frame.add(myCanvas, BorderLayout.CENTER);
			
			frame.setVisible(true);

			animator = new FPSAnimator(canvas, FPS);
	}
	
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == startButton) {
			animate = true;
			animator.start();
		} else if (event.getSource() == stopButton) {
			animate = false;
			animator.stop();
		} else if (event.getSource() == resetButton) {
			reset();
		} else if (event.getSource() == quitButton) { System.exit(0); }
	}
	
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == trailLengthSlider) {
			TRAIL_LENGTH = trailLengthSlider.getValue();
		} else if (e.getSource() == trailSpeedSlider) {
			TRAIL_SPEED = trailSpeedSlider.getValue();
		} else if (e.getSource() == aSlider) {
			A = aSlider.getValue();
		} else if (e.getSource() == bSlider ) {
			B = bSlider.getValue();
		} else if (e.getSource() == cSlider ) {
			C = cSlider.getValue();
		}
	}
	
	
	@Override
	public void keyTyped(KeyEvent e) {
		if( e.getKeyChar() == 'w' )
			viewZ -= 10;
		else if( e.getKeyChar() == 's' )
			viewZ += 10;
		else if( e.getKeyChar() == 'a' )
			viewX -= 10;
		else if( e.getKeyChar() == 'd' )
			viewX += 10;
		else if( e.getKeyCode() == KeyEvent.VK_UP )
			viewY += 10;
		else if( e.getKeyCode() == KeyEvent.VK_DOWN )
			viewY -= 10;
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {}


	@Override
	public void keyReleased(KeyEvent e) {}


	public void display(GLAutoDrawable drawable) {
		update();
		render(drawable);
	}

	
	private void update() {
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		glu.gluLookAt(viewX, viewZ, viewY, 0f, 0f, 0f, 0f, 0f, 1f);  
	}
	
	
	private float[] lorenz(float X, float Y, float Z) {
		float Xt = X + t * A * (Y - X);
		float Yt = Y + t * (X * (B - Z) - Y);
		float Zt = Z + t * (X * Y - C * Z);
		X = Xt;
		Y = Yt;
		Z = Zt;
		float P[] = {X, Y, Z};
		return P;
	}
	
	
	private void reset() {
		A = 10f;
		B = 28f;
		C = 8f / 3f;
		ITER = 20000;
		RADIUS = 0.25f;
		viewX = 5;
		viewY = 90;
		viewZ = 20;
		lastX = 0.1f;
		lastY = 0.1f;
		lastZ = 0.1f;
		TRAIL_LENGTH = 3000;
		TRAIL_SPEED = 450;
		
		aSlider.setValue((int)(A));
		bSlider.setValue((int)(B));
		cSlider.setValue((int)(C));
		trailLengthSlider.setValue((int)(TRAIL_LENGTH));
		trailSpeedSlider.setValue((int)(TRAIL_SPEED));
		
		animate = false;
		animator.stop();
	}

	
	private void render(GLAutoDrawable drawable) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glColor3f(0f, 0f, 0f);
		
		float P[] = new float[3];
		int steps;
		if( animate ) {
			steps = TRAIL_LENGTH;
			P[0] = lastX;
			P[1] = lastY;
			P[2] = lastZ;
		} else {
			steps = ITER;
			P[0] = initX;
			P[1] = initY;
			P[2] = initZ;
		}
		
		for( int i = 0; i < steps; i++ ) {
			float[] hue = {0.2f, 0.2f, 0.2f + ((float) i)/((float) steps)*0.5f};
			gl.glColor3fv(hue, 0);
			
			P = lorenz(P[0],P[1],P[2]);
			
			if( i == TRAIL_SPEED ) {
				lastX = P[0];
				lastY = P[1];
				lastZ = P[2];
			}
			
			gl.glPushMatrix();
				gl.glTranslatef(P[0], P[1], P[2]);
				GLUquadric quad0 = glu.gluNewQuadric();
				glu.gluSphere(quad0, RADIUS, 3, 3);
				glu.gluDeleteQuadric(quad0);
			gl.glPopMatrix();
		}
	}
	
	
	public void dispose(GLAutoDrawable drawable) { /* put the cleanup code here */ }

	
	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();
		glu = new GLU();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(60f, 1f, 0.5f, 200f); 
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		glu.gluLookAt(viewX, viewY, viewZ, 0f, 0f, 0f, 0f, 0f, 1f);
		gl.glClearColor(BG[0], BG[1], BG[2], 1);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	}

	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		gl.glViewport(0, 0, width, height);
		float aspect = width*1.0f/height;
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(60f, aspect, 0.5f, 200f); 
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		
	}
	
}