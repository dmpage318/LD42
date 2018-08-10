package TnT.ld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	
	public Looper gloop, ploop, aloop;
	
	public JFrame frame;
	public JPanel panel;
	public VolatileImage vimg;
	public int width = 1024, height = 768;
	
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
				width = panel.getWidth();
				height = panel.getHeight();
				vimg = panel.createVolatileImage(width, height);
			}
		});
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		panel.addMouseWheelListener(this);
		panel.addKeyListener(this);
		panel.setFocusable(true);
		panel.grabFocus();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		gloop = new Looper(this::graphics, 120).start();
		ploop = new Looper(this::physics, 100).start();
		aloop = new Looper(this::animate, 100).start();
	}
	
	public void graphics(double dt) {
		if (vimg == null) return;
		Graphics2D g = vimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// TODO: graphics
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		
		
		
		
		Animation a;
		for (int i = 0; i < activeAnimations.size(); i++) {
			try {
				a = activeAnimations.get(i);
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
			a.paint(g);
		}
		
		g.dispose();
		g = (Graphics2D) panel.getGraphics();
		g.drawImage(vimg, 0, 0, null);
		g.dispose();
	}
	
	public void physics(double dt) {
		
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

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}
	
}
