package TnT.ld;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Box {
	int x, y;
	int width, height;
	boolean[][] shape;
	Level level;

	boolean ghost;
	Box ghostParent;
	boolean ghostFromVehicle;
	public ConveyorSegment conveyor;
	boolean hasMovedThisTick = false;
	
	public Box(int maxWidth, int maxHeight, Level level) {
		width = maxWidth; 
		height = maxHeight;
		this.level = level;
		shape = new boolean[width][height];
		
		// generate random shape (random-first search?)
		int cells = (int) (Math.random() * width * height) + 1;
		boolean[][] queued = new boolean[width][height];
		List<Point> frontier = new ArrayList<>();
		Point p = new Point((int) (Math.random()*width), (int) (Math.random()*height));
		frontier.add(p);
		queued[p.x][p.y] = true;
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (int i = 0; i < cells; i++) {
			p = frontier.remove((int) (Math.random()*frontier.size()));
			shape[p.x][p.y] = true;
			minx = Math.min(minx, p.x);
			miny = Math.min(miny, p.y);
			maxx = Math.max(maxx, p.x);
			maxy = Math.max(maxy, p.y);
			if (p.x > 0 && !queued[p.x-1][p.y]) {
				frontier.add(new Point(p.x-1, p.y));
				queued[p.x-1][p.y] = true;
			}
			if (p.x < width-1 && !queued[p.x+1][p.y]) {
				frontier.add(new Point(p.x+1, p.y));
				queued[p.x+1][p.y] = true;
			}
			if (p.y > 0 && !queued[p.x][p.y-1]) {
				frontier.add(new Point(p.x, p.y-1));
				queued[p.x][p.y-1] = true;
			}
			if (p.y < height-1 && !queued[p.x][p.y+1]) {
				frontier.add(new Point(p.x, p.y+1));
				queued[p.x][p.y+1] = true;
			}
		}
		
		width = maxx-minx+1;
		height = maxy-miny+1;
		boolean[][] newShape = new boolean[width][height];
		for (int i = 0; i < width; i++)
			newShape[i] = Arrays.copyOfRange(shape[i+minx], miny, maxy+1);
		shape = newShape;
	}
	
	public Box(Box b) {
		x = b.x;
		y = b.y;
		width = b.width;
		height = b.height;
		level = b.level;
		shape = new boolean[width][];
		for (int i = 0; i < width; i++) 
			shape[i] = Arrays.copyOf(b.shape[i], height);
	}
	
	public synchronized void paint(Graphics2D g) {
		Color fillColor = new Color(0xcc, 0xa4, 0x83, ghost?128:255);
		Color lineColor = fillColor.darker();
		g.setStroke(new BasicStroke(2f));
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (shape[i][j]) {
					g.setColor(fillColor);
					g.fillRect(x+i*level.cellSize, y+j*level.cellSize, level.cellSize+1, level.cellSize+1);
					g.setColor(lineColor);
					if (i==0 || !shape[i-1][j])
						g.drawLine(x+i*level.cellSize, y+j*level.cellSize, x+i*level.cellSize, y+(j+1)*level.cellSize);
					if (i==width-1 || !shape[i+1][j])
						g.drawLine(x+(i+1)*level.cellSize, y+j*level.cellSize, x+(i+1)*level.cellSize, y+(j+1)*level.cellSize);
					if (j==0 || !shape[i][j-1])
						g.drawLine(x+i*level.cellSize, y+j*level.cellSize, x+(i+1)*level.cellSize, y+j*level.cellSize);
					if (j==height-1 || !shape[i][j+1])
						g.drawLine(x+i*level.cellSize, y+(j+1)*level.cellSize, x+(i+1)*level.cellSize, y+(j+1)*level.cellSize);
				}
			}
		}
	}
	
	public Box ghostBox() {
		Box g = new Box(this);
		g.ghost = true;
		g.ghostParent = this;
		return g;
	}
	
	public boolean contains(int x, int y) {
		if (x < this.x || y < this.y) return false;
		int cx = (x-this.x)/level.cellSize;
		int cy = (y-this.y)/level.cellSize;
		return cx<width && cy<height && shape[cx][cy];
	}
	
	public boolean intersects(Box box) {
		int cs = level.cellSize;
		for (int i1 = 0; i1 < width; i1++) {
			for (int j1 = 0; j1 < height; j1++) {
				if (!shape[i1][j1]) continue;
				for (int i2 = 0; i2 < box.width; i2++) {
					for (int j2 = 0; j2 < box.height; j2++) {
						if (box.shape[i2][j2] && 
								new Rectangle(x+i1*cs, y+j1*cs, cs, cs).intersects(
										new Rectangle(box.x+i2*cs, box.y+j2*cs, cs, cs))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public synchronized void rotateRight(int centerx, int centery) {
		boolean[][] newShape = new boolean[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				newShape[i][j] = shape[j][height-i-1];
			}
		}
		int newx = centerx - height*level.cellSize + centery-y;
		int newy = centery - centerx+x;
		shape = newShape;
		height = width;
		width = shape.length;
		x = newx;
		y = newy;
	}
	
	public synchronized void rotateLeft(int centerx, int centery) {
		boolean[][] newShape = new boolean[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				newShape[i][j] = shape[width-j-1][i];
			}
		}
		int newx = centerx - centery+y;
		int newy = centery - width*level.cellSize + centerx-x;
		shape = newShape;
		height = width;
		width = shape.length;
		x = newx;
		y = newy;
	}
}
