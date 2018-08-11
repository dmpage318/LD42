package TnT.ld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.VolatileImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import TnT.ld.animation.Animation;
import TnT.ld.util.Looper;

public class LD42 implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	public static final LD42 theLD = new LD42();
	public static final String NAME = "NAME ME PLZ";
	public static final int width = 1024, height = 768;
	
	public Looper gloop, ploop, aloop;
	
	public JFrame frame;
	public JPanel panel;
	public VolatileImage vimg;
	public double scale;
	public int barWidth, barHeight;
	
	Level currentLevel;
	
	public State gameState;
	enum State {
		MAIN, LEVEL
	}
	
	private LD42() {}
	public static void main(String[] args) {
		theLD.start();
	}
	
	public void start() {
		// build frame
		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(width, height));
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int pw = panel.getWidth();
				int ph = panel.getHeight();
				scale = Math.min((double)pw/width, (double)ph/height);
				barWidth = (int) (pw-width*scale)/2;
				barHeight = (int) (ph-height*scale)/2;
				vimg = panel.createVolatileImage(pw, ph);
			}
		});
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		panel.addMouseWheelListener(this);
		panel.addKeyListener(this);
		panel.setFocusable(true);
		panel.setFocusTraversalKeysEnabled(false);
		panel.grabFocus();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		gloop = new Looper(this::graphics, 120).start();
		ploop = new Looper(this::physics, 100).start();
		aloop = new Looper(this::animate, 100).start();
		
		currentLevel = new Level();
		gameState = State.LEVEL;
	}
	
	double frameCountTime = 0;
	int frames = 0;
	int fps = 0;
	public void graphics(double dt) {
		if (vimg == null) return;
		Graphics2D g = vimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		frames++;
		if ((frameCountTime += dt) > 1) {
			fps = frames;
			frameCountTime = 0;
			frames = 0;
		}
		
		int pw = panel.getWidth();
		int ph = panel.getHeight();
		g.translate(barWidth, barHeight);
		g.scale(scale, scale);
		
		//------------------------------------------------
		// Graphics scaled to default windows size
		//------------------------------------------------
		// TODO: graphics
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		if (gameState == State.LEVEL && currentLevel != null) currentLevel.paint(g);
		
		Animation a;
		for (int i = 0; i < activeAnimations.size(); i++) {
			try {
				a = activeAnimations.get(i);
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
			a.paint(g);
		}
		//-----------------------------------------------
		
		g.scale(1/scale, 1/scale);
		g.translate(-barWidth, -barHeight);
		g.setColor(Color.black);
		g.fillRect(0, 0, (int) barWidth, ph);
		g.fillRect(pw - (int) barWidth, 0, (int) barWidth, ph);
		g.fillRect(0, 0, pw, (int) barHeight);
		g.fillRect(0, ph - (int) barHeight, pw, (int) barHeight);
		
		g.setColor(Color.red);
		g.drawString(fps+"", 1, 11);
		
		g.dispose();
		g = (Graphics2D) panel.getGraphics();
		g.drawImage(vimg, 0, 0, null);
		g.dispose();
	}
	
	double timeToNextBox = 5;
	public void physics(double dt) {
		if(currentLevel != null && currentLevel.conveyors != null) {
			for(ConveyorSegment s : currentLevel.conveyors) {
				if (s.box != null) {
					s.box.hasMovedThisTick = false;
				}
			}
			for(ConveyorSegment s : currentLevel.conveyors) {
				s.physics();
			}

			timeToNextBox -= dt;
			if(timeToNextBox <= 0) {
				currentLevel.newBox();
				timeToNextBox = 5;
			}
		}
		
	}
	
	public Queue<Animation> newAnimations = new LinkedList<>();
	private List<Animation> activeAnimations = Collections.synchronizedList(new LinkedList<>());
	public void animate(double dt) {
		while (!newAnimations.isEmpty()) {
			Animation a = newAnimations.remove();
			a.initialize();
			activeAnimations.add(a);
		}
		for (Iterator<Animation> iter = activeAnimations.iterator(); iter.hasNext();) {
			Animation a = iter.next();
			if (a.isStopped() || a.isFinished()) {
				iter.remove();
				a.finish();
				a.stop();
			} else {
				a.update(dt);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if (gameState == State.LEVEL && currentLevel != null) {
			int x = e.getX();
			int y = e.getY();
			if (x >= barWidth && x < panel.getWidth()-barWidth && y >= barHeight && y < panel.getHeight()-barHeight)
				currentLevel.mousePressed((int)((x-barWidth)/scale), (int)((y-barHeight)/scale));
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (gameState == State.LEVEL && currentLevel != null) {
			int x = e.getX();
			int y = e.getY();
			if (x >= barWidth && x < panel.getWidth()-barWidth && y >= barHeight && y < panel.getHeight()-barHeight)
				currentLevel.mouseReleased((int)((x-barWidth)/scale), (int)((y-barHeight)/scale));
		}
	}
	public void mouseMoved(MouseEvent e) {
		if (gameState == State.LEVEL && currentLevel != null) {
			int x = e.getX();
			int y = e.getY();
			if (x >= barWidth && x < panel.getWidth()-barWidth && y >= barHeight && y < panel.getHeight()-barHeight)
				currentLevel.mouseMoved((int)((x-barWidth)/scale), (int)((y-barHeight)/scale));
		}
	}
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
	public void keyPressed(KeyEvent e) {
		if (gameState == State.LEVEL && currentLevel != null) {
			currentLevel.keyPressed(e);
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}
	
}
