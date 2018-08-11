package TnT.ld;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import TnT.ld.animation.ConveyorAnimator;

public class Level {
	int cellSize = 40;
	Vehicle vehicle;
	List<Box> freeBoxes = new ArrayList<>();
	Box mouseCapturedBox;
	Point mouseCaptureOffset;
	boolean readyToDrop = true;
	ArrayList<ConveyorSegment> conveyors = new ArrayList<>();
	
	public Level() {
		vehicle = new Vehicle(10, 8, this);
		int dx, dy;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				Box b = new Box(3, 3, this);
				b.x = 20 + ConveyorSegment.width*i;
				b.y = 20 + ConveyorSegment.height*j;
				freeBoxes.add(b);
				dx = 0;
				dy = 0;
				if (j % 2 == 1) {
					if (i % 2 == 0) {
						dx = 1;
					} else {
						dy = -1;
					}
				} else {
					if (i % 2 == 0) {
						dy = -1;
					} else {
						dx = -1;
					}
				}
				conveyors.add(new ConveyorSegment(20 + ConveyorSegment.width * i, 20 + ConveyorSegment.height * j, dx, dy));
			}
		}
		
		//Add permanent ones across the top.
		for(int x = 20 + 2 * ConveyorSegment.width; x < LD42.width; x+= ConveyorSegment.width) {
			conveyors.add(new ConveyorSegment(x, 20, -1, 0, true));			
		}
		LD42.theLD.newAnimations.add(new ConveyorAnimator(this.conveyors));
	}
	
	public void paint(Graphics2D g) {
		vehicle.hover(mouseCapturedBox);
		vehicle.paint(g);
		for (int i = 0; i < freeBoxes.size(); i++)
			freeBoxes.get(i).paint(g);
		for (int i = 0; i < conveyors.size(); i++) {
			conveyors.get(i).paint(g);
		}
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
