package TnT.ld;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import TnT.ld.animation.VehicleEnterAnimation;
import TnT.ld.animation.VehicleExitAnimation;


public class Level {
	public boolean gameOver = false;
	double boxDT = 3.5;
	public boolean showGameOver = false;
	public int cellSize = 40;
	volatile int x, y;
	public Vehicle vehicle;
	List<Box> freeBoxes = new ArrayList<>();
	Box ghostBox;
	Point ghostBoxOffset;
	boolean readyToDrop = true;
	ArrayList<ConveyorSegment> conveyors = new ArrayList<>();
	ConveyorSegment first = null;
	public int vehicleX, vehicleY;
	public boolean shipmentInProgress;
	public int vehicleCount = 0;
	public Level() {
		int dx, dy;
		/*
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
//				Box b = new Box(3, 3, this);
//				b.x = 20 + ConveyorSegment.width*i;
//				b.y = 20 + ConveyorSegment.height*j;
//				freeBoxes.add(b);
				dx = 0;
				dy = 0;
				if (j % 2 == 1) {
					if (i % 2 == 0) {
						dx = 1;
					} else {
						dy = 1;
					}
				} else {
					if (i % 2 == 0) {
						dy = 1;
					} else {
						dx = -1;
					}
				}
				ConveyorSegment c = new ConveyorSegment(20 + ConveyorSegment.width * i, 20 + ConveyorSegment.height * j, dx, dy);
				if(i == 0 && j == 4) {
					c.on = false;
				}
				conveyors.add(c);
			}
		}


		conveyors.get(5).setNext(conveyors.get(0));
		conveyors.get(0).setNext(conveyors.get(1));
		conveyors.get(1).setNext(conveyors.get(6));
		conveyors.get(6).setNext(conveyors.get(7));
		conveyors.get(7).setNext(conveyors.get(2));
		conveyors.get(2).setNext(conveyors.get(3));
		conveyors.get(3).setNext(conveyors.get(8));
		conveyors.get(8).setNext(conveyors.get(9));
		conveyors.get(9).setNext(conveyors.get(4));
		 */
		int padding = (LD42.height-5*ConveyorSegment.height)/2;
		for(int i = 4; i >= 0; i--) {
			ConveyorSegment c = new ConveyorSegment(padding, padding + (ConveyorSegment.height+1) * i, 0, 1, false);
			if(i < 4) {
				c.setNext(conveyors.get(3-i));
			}
			conveyors.add(c);
		}


		//Add permanent ones across the top.
		ConveyorSegment last = null;
		for(int x = padding+1 + ConveyorSegment.width; x < LD42.width; x+= ConveyorSegment.width) {
			ConveyorSegment next = new ConveyorSegment(x, padding, -1, 0, true);
			next.setNext( last == null ? conveyors.get(4) : last);
			last = next;
			conveyors.add(last);
		}
		first = last; //the last one added is the first to get boxes

		newBox();

		vehicleX = padding + ConveyorSegment.width*2; // left side of the vehicle
		vehicleY = (LD42.height + ConveyorSegment.height)/2; // center of the vehicle
		vehicle = new Vehicle(10, 8, this);
		vehicle.x = vehicleX;
	}

	public void newBox() {
		Box b = new Box(3, 3, this);
		b.x = first.x + 5 + (3-b.width)*cellSize;
		b.y = first.y + 5;
		freeBoxes.add(b);
		first.setBox(b);
		timeToNextBox = Math.max(2.5, boxDT);
		boxDT -= .25;
	}

	public void paint(Graphics2D g) {
		g.setColor(Color.RED);
		g.setFont(new Font("Tahoma", Font.PLAIN, 25));
		g.drawString("Click to select a box and place it on the Truck. Don't let the boxes back up!", 60, 30);
		g.setFont(new Font("Tahoma", Font.BOLD, 20));
		g.drawString("Trucks sent: " + vehicleCount, 314, 200);
		if (showGameOver) {
			g.setFont(new Font("Tahoma", Font.BOLD, 40));
			g.drawString("Game Over!", 314, 400);
		}
//		g.setFont(new Font("Tahoma", Font.PLAIN, 20));
//		g.drawString(String.format("Next box in %.1fs", timeToNextBox), 800, 200);
		g.setFont(new Font("Tahoma", Font.BOLD, 20));
		vehicle.hover(ghostBox);
		vehicle.paint(g);

		for (int i = 0; i < conveyors.size(); i++) {
			conveyors.get(i).paint(g);
		}
		for (int i = 0; i < freeBoxes.size(); i++) {
			freeBoxes.get(i).paint(g, i + 1);
		}
		if (ghostBox != null) {
			ghostBox.paint(g, -1);
			g.setColor(Color.RED);
			g.drawString("[tab]", 190, vehicleY);
			g.drawString("to rotate", 190, vehicleY + 30);
		}
	}

	double timeToNextBox = 0;
	public void physics(double dt) {
		if (conveyors != null) {
			for (int i = 0; i < freeBoxes.size(); i++) { 
				freeBoxes.get(i).hasMovedThisTick = false;
			}
			for (ConveyorSegment s : conveyors) {
				s.on = s.permanentOn || s.boxes.isEmpty() || s.next != null && (s.next.on || s.next.boxes.isEmpty());
			}
			for (ConveyorSegment s : conveyors) {
				s.physics();
			}
			for (int i = 0; i < freeBoxes.size(); i++) {
				for (int j = 0; j < i; j++) {
					if (freeBoxes.get(i).intersects(freeBoxes.get(j))) {
						freeBoxes.get(i).dead = true;
						freeBoxes.get(j).dead = true;
						gameOver = true;
						new VehicleExitAnimation(this).start();
						ghostBox = null;
						Thread.currentThread().suspend();
					}
				}
			}

			timeToNextBox -= dt;
			if(timeToNextBox <= 0) {
				newBox();
			}
		}
	}

	public void mousePressed(int x, int y) {
		if (gameOver) {
			return;
		}
		if (ghostBox != null) {
			// already holding a box
			readyToDrop = true;
			return;
		}
		for (int i = 0; i < freeBoxes.size(); i++)
			if (freeBoxes.get(i).contains(x, y)) {
				ghostBox = freeBoxes.get(i).ghostBox();
				break;
			}
		if (ghostBox == null)
			for (int i = 0; i < vehicle.packedBoxes.size(); i++)
				if (vehicle.packedBoxes.get(i).contains(x, y)) {
					ghostBox = vehicle.ghost(i);
					break;
				}
		if (ghostBox != null) {
			ghostBox.ghost = true;
			ghostBoxOffset = new Point(x-ghostBox.x, y-ghostBox.y);
		}
		readyToDrop = false;
	}

	public void mouseReleased(int x, int y) {
		if (gameOver) {
			return;
		}
		if (ghostBox != null && readyToDrop) {
			Point pv = vehicle.fit(ghostBox);
			if (pv != null) {
				if (ghostBox.ghostFromVehicle) vehicle.remove(ghostBox.ghostParent);
				else {
					freeBoxes.remove(ghostBox.ghostParent);
					if(ghostBox.ghostParent != null && ghostBox.ghostParent.conveyor != null) {
						ghostBox.ghostParent.conveyor.boxRemoved(ghostBox.ghostParent);
					}

				}
				vehicle.add(ghostBox, pv);
				ghostBox.ghost = false;
			} else if (ghostBox.ghostFromVehicle) {
				vehicle.cancelGhost(ghostBox);
			}
			ghostBox = null;
		}
	}

	public void mouseMoved(int x, int y) {
		if (gameOver) {
			return;
		}
		this.x = x;
		this.y = y;
		if (ghostBox != null) {
			ghostBox.x = x - ghostBoxOffset.x;
			ghostBox.y = y - ghostBoxOffset.y;
		}
		readyToDrop = true;
	}

	public void shipIt() {
		int fill = 0;
		for (int i = 0; i < vehicle.filled.length; i++) {
			for (int j = 0; j < vehicle.filled[i].length; j++) {
				if (vehicle.filled[i][j]) {
					fill++;
				}
			}
		}
		int missed = vehicle.filled.length * vehicle.filled[0].length - fill;
		System.out.println("Old vehicle had " + fill +" filled and " + missed + " missed");
		ArrayList<Point> sizes = new ArrayList<Point>();
		int ceil = 15;
		for (int i = 3; i <= ceil; i++) {
			for (int j = Math.max((int) Math.floor(fill / i) - 1, 3); j <= ceil; j++) {
				if (i * j >= fill) { //only add it if it is big enough
					sizes.add(new Point(i, j));
				}
			}
		}
		sizes.sort((x,y)->{ //sort based on the one that is closest to our size
			return x.x*x.y - y.x*y.y;
		});
		final int initWidth = sizes.get(0).x;
		final int initHeight = sizes.get(0).y;
		Point ideal = sizes.stream().filter((x)->{ //get the ones that match our size
			return x.x*x.y == initWidth * initHeight;
		}).sorted((x,y)->{ //get the most square one if there are multiple
			return Math.abs(x.x-x.y) - Math.abs(y.x - y.y);
		}).collect(Collectors.toList()).get(0);
		int width = ideal.x;
		int height = ideal.y;
		if (height > width) {
			int temp = width;
			width = height;
			height = temp;
		}
		if (width == 3 && height == 3 && fill < 9) { //we need to see about making it smaller
			switch (fill) {
			case 8:
			case 7:
				width = 4;
				height = 2;
				break;
			case 6:
			case 5:
				width = 3;
				height = 2;
				break;
			case 4:
				width = 2;
				height = 2;
				break;
			case 3:
			case 2:
			case 1:
				width = fill;
				height = 1;
			}
		}
		System.out.println("New Vehicle has size: " + width + ", " + height);
		Vehicle nv = new Vehicle(width, height, this);
		vehicle = nv;
		vehicleCount++;
		new VehicleEnterAnimation(this).start();
	}

	public void keyPressed(KeyEvent e) {
		if (gameOver) {
			return;
		}
		switch(e.getKeyCode()) {
		case  KeyEvent.VK_TAB: //rotate
			if (ghostBox != null) {
				int cx = ghostBox.x + ghostBoxOffset.x;
				int cy = ghostBox.y + ghostBoxOffset.y;
				ghostBox.rotateRight(cx, cy);
				ghostBoxOffset = new Point(cx-ghostBox.x, cy-ghostBox.y);
			}
			break;
		case KeyEvent.VK_SPACE: //ship it!
			//shipIt();
			if (!shipmentInProgress) {
				int fill = 0;
				for (int i = 0; i < vehicle.filled.length; i++) {
					for (int j = 0; j < vehicle.filled[i].length; j++) {
						if (vehicle.filled[i][j]) {
							fill++;
						}
					}
				}
				if (fill > 0) {
					new VehicleExitAnimation(this).start();
				} else {
					vehicle.cannotEmpty = 150;
				}
			}
			break;
		}
		if (Character.isDigit(e.getKeyChar())) { //doing it this way allows numpad use too
			int index = (e.getKeyChar() - '0') - 1;
			if (index < freeBoxes.size()) {
				Box box = freeBoxes.get(index);
				if (box.x > 1024) {
					return;
				}
				ghostBox = box.ghostBox();
				ghostBox.ghost = true;
				ghostBoxOffset = new Point(ghostBox.width * cellSize / 2, ghostBox.height * cellSize / 2);
				ghostBox.x = x - ghostBoxOffset.x;
				ghostBox.y = y - ghostBoxOffset.y;
			}
		}
	}

}
