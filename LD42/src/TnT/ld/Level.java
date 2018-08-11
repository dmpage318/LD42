package TnT.ld;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Level {
	int cellSize = 40;
	Vehicle vehicle;
	List<Box> freeBoxes = new ArrayList<>();
	Box mouseCapturedBox;
	Point mouseCaptureOffset;
	boolean readyToDrop = true;
	
	public Level() {
		vehicle = new Vehicle(10, 8, this);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
			Box b = new Box(3, 3, this);
			b.x = 20 + 140*i;
			b.y = 20 + 140*j;
			freeBoxes.add(b);
			}
		}
	}
	
	public void paint(Graphics2D g) {
		vehicle.hover(mouseCapturedBox);
		vehicle.paint(g);
		for (int i = 0; i < freeBoxes.size(); i++)
			freeBoxes.get(i).paint(g);
		if (mouseCapturedBox != null) mouseCapturedBox.paint(g);
	}
	
	public void mousePressed(int x, int y) {
		if (mouseCapturedBox != null) {
			// already holding a box
			readyToDrop = true;
			return;
		}
		for (int i = 0; i < freeBoxes.size(); i++)
			if (freeBoxes.get(i).contains(x, y)) {
				mouseCapturedBox = freeBoxes.remove(i);
				break;
			}
		if (mouseCapturedBox == null)
			for (int i = 0; i < vehicle.packedBoxes.size(); i++)
				if (vehicle.packedBoxes.get(i).contains(x, y)) {
					mouseCapturedBox = vehicle.remove(i);
					break;
				}
		if (mouseCapturedBox != null) {
			mouseCapturedBox.lifted = true;
			mouseCaptureOffset = new Point(x-mouseCapturedBox.x, y-mouseCapturedBox.y);
		}
		readyToDrop = false;
	}
	
	public void mouseReleased(int x, int y) {
		if (mouseCapturedBox != null && readyToDrop) {
			Point pv = vehicle.fit(mouseCapturedBox);
			if (pv != null) vehicle.add(mouseCapturedBox, pv);
			else freeBoxes.add(mouseCapturedBox);
			mouseCapturedBox.lifted = false;
			mouseCapturedBox = null;
		}
	}
	
	public void mouseMoved(int x, int y) {
		if (mouseCapturedBox != null) {
			mouseCapturedBox.x = x - mouseCaptureOffset.x;
			mouseCapturedBox.y = y - mouseCaptureOffset.y;
		}
		readyToDrop = true;
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_TAB) {
			if (mouseCapturedBox != null) {
				int cx = mouseCapturedBox.x + mouseCaptureOffset.x;
				int cy = mouseCapturedBox.y + mouseCaptureOffset.y;
				mouseCapturedBox.rotateRight(cx, cy);
				mouseCaptureOffset = new Point(cx-mouseCapturedBox.x, cy-mouseCapturedBox.y);
			}
		}
	}
	
}
