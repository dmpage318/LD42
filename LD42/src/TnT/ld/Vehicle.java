package TnT.ld;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Vehicle {
	public int x, y;
	public int width, height;
	Level level;
	List<Box> packedBoxes = new ArrayList<>();
	List<Point> packedLocation = new ArrayList<>();
	public boolean[][] filled;
	Box hoverBox;
	Point hoverLocation;
	
	public static double speed = 2000, accel = 4000;
	
	public Vehicle(int width, int height, Level level) {
		this.width = width;
		this.height = height;
		this.level = level;
		filled = new boolean[width][height];
		
		// TODO: come up with the location. start offscreen then move in?
		x = 0;
		y = level.vehicleY - height*level.cellSize/2;
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
			b.paint(g, -1);
		}
		//draw the truck-y bits
		int wheelHeight = level.cellSize * 5 / 8;
		int wheelWidth = level.cellSize * 3 / 2;
		int frontHeight = Math.min( 5 *level.cellSize, height * level.cellSize * 7 / 8);
		frontHeight = height * level.cellSize;
		int frontWidth = Math.min(frontHeight * 3 / 4, 2 * level.cellSize);
		int engineWidth = frontWidth * 4 / 3;
		int engineHeight = frontHeight * 5 / 8;
		engineHeight = frontHeight - 20;
		
		int filledCount = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if(filled[i][j]) {
					filledCount++;
				}
			}
		}
		if (!level.shipmentInProgress) {
			g.setColor(Color.red);
			g.setFont(new Font("Tahoma", Font.BOLD, 20));
			g.drawString(String.format("Vehicle total size: %d, Amount filled: %d, Space left: %d (%.1f%%)", width * height, filledCount, width * height - filledCount, (double) 100*(width * height - filledCount) / (width*height)), x, y - 60);
			g.drawString("Press [space] to get new truck.",  x, y + height * level.cellSize + 60);
			g.drawString("It will be smaller based on unused space.",  x, y + height * level.cellSize + 90);
		}
		//wheels on the front bit
		drawWheel(g, x + width * level.cellSize + frontWidth + (int) (engineWidth - wheelWidth) / 2, y + (height * level.cellSize - engineHeight) / 2 - wheelHeight / 2, wheelWidth, wheelHeight);
		drawWheel(g, x + width * level.cellSize + frontWidth + (int) (engineWidth - wheelWidth) / 2, y + (height * level.cellSize - engineHeight) / 2 + engineHeight - wheelHeight / 2, wheelWidth, wheelHeight);
		g.setColor(Color.GRAY);
		Shape front = new RoundRectangle2D.Float(x + width * level.cellSize + frontWidth, y + (height * level.cellSize - engineHeight) / 2, engineWidth, engineHeight, 30, 40);
		g.fill(front);
		g.setColor(Color.GRAY);
		g.fillRect(x + width * level.cellSize + frontWidth, y + (height * level.cellSize - engineHeight) / 2, engineWidth / 2, engineHeight);
		g.setColor(Color.DARK_GRAY);
		Shape curClip = g.getClip();
		Stroke curStroke = g.getStroke();
		g.setClip(front);
		int[] taperX = new int[] {x + width * level.cellSize + frontWidth + engineWidth / 10, x + width * level.cellSize + frontWidth + engineWidth, x + width * level.cellSize + frontWidth + engineWidth};
		int[] taperYUp = new int[] {y + (height * level.cellSize - engineHeight) / 2, y + (height * level.cellSize - engineHeight) / 2, y + (height * level.cellSize - engineHeight) / 2 + engineHeight / 5};
		int[] taperYDown = new int[] {y + (height * level.cellSize - engineHeight) / 2 + engineHeight, y + (height * level.cellSize - engineHeight) / 2 + engineHeight, y + (height * level.cellSize - engineHeight) / 2 + engineHeight - engineHeight / 5};
		g.setColor(Color.DARK_GRAY.brighter());
		g.fillPolygon(taperX, taperYUp, taperX.length);
		g.fillPolygon(taperX, taperYDown, taperX.length);
		g.setClip(curClip);
		g.setStroke(curStroke);
		//wheels
		drawWheel(g, x + level.cellSize * width / 8 - level.cellSize / 2, y - wheelHeight, wheelWidth, wheelHeight);
		drawWheel(g, x + level.cellSize * 3 * width / 4 - level.cellSize / 2, y - wheelHeight, wheelWidth, wheelHeight);
		drawWheel(g, x + level.cellSize * width / 8 - level.cellSize / 2, y + level.cellSize * height, wheelWidth, wheelHeight);
		drawWheel(g, x + level.cellSize * 3 * width / 4 - level.cellSize / 2, y + level.cellSize * height, wheelWidth, wheelHeight);
		//front of truck
//		g.setColor(new Color(222, 184, 135));
		g.setColor(new Color(200, 100, 100));
		g.fill(new RoundRectangle2D.Float(x + width * level.cellSize, y + (height * level.cellSize - frontHeight) / 2, frontWidth, frontHeight, 30, 10));
		g.fillRect(x + width * level.cellSize, y + (height * level.cellSize - frontHeight) / 2, frontWidth / 2, frontHeight);
		//lights on top
		g.setColor(Color.ORANGE);
		for (int i = 0; i < 3; i++) {
			g.fillOval(x + width * level.cellSize + frontWidth - level.cellSize, y + (height * level.cellSize - frontHeight) / 2 + (2+i) * frontHeight / 6 - 2, 10, 4);
		}
		g.setColor(Color.DARK_GRAY);
		int[] windshieldX = new int[] {x + width * level.cellSize + frontWidth - level.cellSize / 2, x + width * level.cellSize + frontWidth, x + width * level.cellSize + frontWidth,x + width * level.cellSize + frontWidth - level.cellSize / 2};
		int[] windshieldY = new int[] {y + (height * level.cellSize - frontHeight) / 2 + level.cellSize / 8, y + (height * level.cellSize - frontHeight) / 2 + level.cellSize / 4, y + (height * level.cellSize - frontHeight) / 2 - level.cellSize / 4 + frontHeight, y + (height * level.cellSize - frontHeight) / 2 - level.cellSize / 8 + frontHeight};
		g.fillPolygon(windshieldX, windshieldY, windshieldX.length);
		
		//outline of back
		g.setStroke(new BasicStroke(3f));
		g.setColor(Color.DARK_GRAY);
		g.drawRect(x, y, width * level.cellSize, height * level.cellSize);
		
		//hoverbox
		if (hoverBox != null) {
			// highlight the region a hovering box will snap to
			Color fillColor = new Color(0, 0.75f, 0, 0.2f);
			Color lineColor = new Color(0, 0.75f, 0, 1);
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
	
	public void drawWheel(Graphics2D g, int x, int y, int wheelWidth, int wheelHeight) {
		Shape origClip = g.getClip();
		g.setClip(x, y, wheelWidth, wheelHeight);
		boolean tog = false;
		for (int xx = x; xx <= x + wheelWidth; xx+= wheelWidth / 10) {
			g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
			tog = !tog;
			g.fillRect(xx, y, wheelWidth / 10, wheelHeight / 2);
			g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
			tog = !tog;
			g.fillRect(xx, y + wheelHeight / 2, wheelWidth / 10, wheelHeight / 2);
			tog = !tog;
		}
		g.setClip(origClip);
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
	
	public void remove(Box b) {
		remove(packedBoxes.indexOf(b));
	}
	
	public Box ghost(int index) {
		Box b = packedBoxes.get(index);
		Point loc = packedLocation.get(index);
		for (int i = 0; i < b.width; i++)
			for (int j = 0; j < b.height; j++)
				if (b.shape[i][j]) filled[loc.x+i][loc.y+j] = false;
		Box ghost = b.ghostBox();
		ghost.ghostFromVehicle = true;
		return ghost;
	}
	
	public void cancelGhost(Box ghost) {
		Box b = ghost.ghostParent;
		Point loc = packedLocation.get(packedBoxes.indexOf(b));
		for (int i = 0; i < b.width; i++)
			for (int j = 0; j < b.height; j++)
				if (b.shape[i][j]) filled[loc.x+i][loc.y+j] = true;
	}
}