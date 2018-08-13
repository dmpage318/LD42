package TnT.ld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 * My idea is that we chain together conveyor segments so that way we can make whatever shape conveyor belt we want by chaining these together.
 * The segments take a box and move it in whatever direction it is supposed to if the segment in front of it is able to receive it (aka the line is not backed up that far).
 * @author Trey
 *
 */
public class ConveyorSegment {
	public static int width = 140;
	public static int height = 140;
	public int x, y, dx, dy;
	public boolean on = true;
	public List<Box> boxes = new ArrayList<>();//Own the box until it is completely contained in next.
	public boolean permanentOn = false;
	public ConveyorSegment next;
	public static int BAR_WIDTH = 10;
	public int startDrawingX = 0;
	public ConveyorSegment prev;
	public ConveyorSegment(int x, int y, int dx, int dy) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
	}
	public ConveyorSegment(int x, int y, int dx, int dy, boolean permanent) {
		this(x,y,dx,dy);
		this.permanentOn = permanent;
	}
	
	
	public void paint(Graphics2D g) {
//		g.setColor(on ? Color.GREEN : Color.BLACK);
		g.setColor(Color.BLACK);
		g.fillRect(x, y, width, height);
		Shape currentClip = g.getClip();
		g.setClip(x, y, width, height);
		Color c1 = Color.LIGHT_GRAY;
		if (permanentOn) {
			c1 = new Color(220, 190, 70);
		}
		if (!on) {
			c1 = Color.GRAY;
		}
		Color c2 = c1.darker();
		if(dx != 0) {
			boolean tog = false;
			if(dx < 0) {
				for(int x = startDrawingX; x < width; x += BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? c1 : c2);
					g.fillRect(this.x+x, this.y+1, BAR_WIDTH, height-2);
				}			
			} else {
				for(int x = width - startDrawingX; x >=-BAR_WIDTH; x -= BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? c1 : c2);
					g.fillRect(this.x+x, this.y+1, BAR_WIDTH, height-2);
				}	
			}
		} else if(dy != 0) {
			boolean tog = false;
			if(dy < 0) {
				for(int y = startDrawingX; y < height; y += BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? c1 : c2);
					g.fillRect(this.x+1, this.y+y, width-2, BAR_WIDTH);
				}	
			} else {
				for(int y = height - startDrawingX; y >= -BAR_WIDTH; y -= BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? c1 : c2);
					g.fillRect(this.x+1, this.y+y, width-2, BAR_WIDTH);
				}	
			}
		}
//		g.setColor(Color.RED);
//		g.drawString("dx: "+dx+" dy: "+dy, x+20, y+20);
		g.setClip(currentClip);
		//NO DIAGONAL!
	}
	public boolean contains(Box b) {	
		return new Rectangle(x,y,width,height).contains(b.x,  b.y,  b.level.cellSize * b.width, b.level.cellSize * b.height);
	}
	public void physics() {
		if(on) {
			startDrawingX -= 1; 
			if(startDrawingX <= -2 * BAR_WIDTH) {
				startDrawingX = 0;
			}
			for (int i = 0; i < boxes.size(); i++) {
				Box box = boxes.get(i);
				if (!box.hasMovedThisTick) {
					box.x += dx;
					box.y += dy;
					box.hasMovedThisTick = true;
					if(next != null && next.contains(box)) {
						next.setBox(box);
						boxes.remove(i--);
					}
				}
			}
		}
	}
	public void boxRemoved(Box box) {
		box.conveyor = null;
		boxes.remove(box);
	}
	public void setNext(ConveyorSegment conveyorSegment) {
		next = conveyorSegment;
		conveyorSegment.prev = this;
	}
	public void setBox(Box b) {
		boxes.add(b);
		b.conveyor = this;
	}
}
