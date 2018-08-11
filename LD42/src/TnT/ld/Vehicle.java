package TnT.ld;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Vehicle {
	int x, y;
	int width, height;
	Level level;
	List<Box> packedBoxes = new ArrayList<>();
	List<Point> packedLocation = new ArrayList<>();
	boolean[][] filled;
	Box hoverBox;
	Point hoverLocation;
	
	public Vehicle(int width, int height, Level level) {
		this.width = width;
		this.height = height;
		this.level = level;
		filled = new boolean[width][height];
		
		// TODO: come up with the location. start offscreen then move in?
		x = LD42.width - width*level.cellSize - 10;
		y = (LD42.height - height*level.cellSize)/2;
	}
	
	public void paint(Graphics2D g) {
		// draw the grid
		g.setStroke(new BasicStroke(1f));
		g.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				g.drawRect(x + i*level.cellSize, y + j*level.cellSize, level.cellSize, level.cellSize);
			}
		}
		
		// update the location of boxes packed into the grid and draw them
		for (int i = 0; i < packedBoxes.size(); i++) {
			Box b = packedBoxes.get(i);
			Point loc = packedLocation.get(i);
			b.x = x + loc.x*level.cellSize;
			b.y = y + loc.y*level.cellSize;
			b.paint(g);
		}
		
		if (hoverBox != null) {
			// highlight the region a hovering box will snap to
			Color fillColor = new Color(1, 0, 0, 0.2f);
			Color lineColor = Color.RED;
			g.setStroke(new BasicStroke(2f));
			for (int i = 0; i < hoverBox.width; i++) {
				for (int j = 0; j < hoverBox.height; j++) {
					if (hoverBox.shape[i][j]) {
						g.setColor(fillColor);
						g.fillRect(x+(hoverLocation.x+i)*level.cellSize, y+(hoverLocation.y+j)*level.cellSize, level.cellSize+1, level.cellSize+1);
						g.setColor(lineColor);
						if (i==0 || !hoverBox.shape[i-1][j])
							g.drawLine(x+(hoverLocation.x+i)*level.cellSize, y+(hoverLocation.y+j)*level.cellSize, x+(hoverLocation.x+i)*level.cellSize, y+(hoverLocation.y+j+1)*level.cellSize);
						if (i==hoverBox.width-1 || !hoverBox.shape[i+1][j])
							g.drawLine(x+(hoverLocation.x+i+1)*level.cellSize, y+(hoverLocation.y+j)*level.cellSize, x+(hoverLocation.x+i+1)*level.cellSize, y+(hoverLocation.y+j+1)*level.cellSize);
						if (j==0 || !hoverBox.shape[i][j-1])
							g.drawLine(x+(hoverLocation.x+i)*level.cellSize, y+(hoverLocation.y+j)*level.cellSize, x+(hoverLocation.x+i+1)*level.cellSize, y+(hoverLocation.y+j)*level.cellSize);
						if (j==hoverBox.height-1 || !hoverBox.shape[i][j+1])
							g.drawLine(x+(hoverLocation.x+i)*level.cellSize, y+(hoverLocation.y+j+1)*level.cellSize, x+(hoverLocation.x+i+1)*level.cellSize, y+(hoverLocation.y+j+1)*level.cellSize);
					}
				}
			}
		}
	}
	
	public void hover(Box b) {
		if (b==null) {
			hoverBox = null;
			hoverLocation = null;
			return;
		}
		hoverLocation = fit(b);
		if (hoverLocation != null) hoverBox = b;
		else hoverBox = null;
	}
	
	public Point fit(Box b) {
		// try to snap a box into the grid, returns null if not
		int sx = (int) Math.round((double) (b.x-x)/level.cellSize);
		int sy = (int) Math.round((double) (b.y-y)/level.cellSize);
		for (int i = 0; i < b.width; i++) {
			for (int j = 0; j < b.height; j++) {
				if (b.shape[i][j]) {
					if (sx+i < 0 || sx+i >= width || sy+j < 0 || sy+j >= height || filled[sx+i][sy+j]) {
						return null;
					}
				}
			}
		}
		return new Point(sx, sy);
	}
	
	public void add(Box b, Point loc) {
		// adds a box given its location (acquired by fit function above, x and y are cell indices)
		packedBoxes.add(b);
		packedLocation.add(loc);
		for (int i = 0; i < b.width; i++)
			for (int j = 0; j < b.height; j++)
				if (b.shape[i][j]) filled[loc.x+i][loc.y+j] = true;
	}
	
	public Box remove(int index) {
		Box b = packedBoxes.remove(index);
		Point loc = packedLocation.remove(index);
		for (int i = 0; i < b.width; i++)
			for (int j = 0; j < b.height; j++)
				if (b.shape[i][j]) filled[loc.x+i][loc.y+j] = false;
		return b;
	}
}
