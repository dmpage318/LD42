package TnT.ld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

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
	public Box box;//Own the box until it is completely contained in next.
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
		if(dx != 0) {
			boolean tog = false;
			if(dx < 0) {
				for(int x = startDrawingX; x < width; x += BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
					g.fillRect(this.x+x, this.y+1, BAR_WIDTH, height-2);
				}			
			} else {
				for(int x = width - startDrawingX; x >=-BAR_WIDTH; x -= BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
					g.fillRect(this.x+x, this.y+1, BAR_WIDTH, height-2);
				}	
			}
		} else if(dy != 0) {
			boolean tog = false;
			if(dy < 0) {
				for(int y = startDrawingX; y < height; y += BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
					g.fillRect(this.x+1, this.y+y, width-2, BAR_WIDTH);
				}	
			} else {
				for(int y = height - startDrawingX; y >= -BAR_WIDTH; y -= BAR_WIDTH, tog = !tog) {
					g.setColor(tog ? Color.LIGHT_GRAY : Color.DARK_GRAY);
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
			if(box != null && !box.hasMovedThisTick) {
				box.x += dx;
				box.y += dy;
				box.hasMovedThisTick = true;
				if(next != null && next.contains(box)) {
					if(next.box != null) {
						System.out.println("YOU SUCK!!!!!!!");
						System.out.println(this);
						System.out.println(this.box);
						Thread.currentThread().suspend();
					}
					next.setBox(box);
					box = null;
					if(!next.on && !permanentOn) {
						on = false;
					}
				}
			}
		}
	}
	public void boxRemoved() {
		box.conveyor = null;
		this.box = null;
		if(!this.on) {
			
			ConveyorSegment back = prev;
			while(back != null && !back.permanentOn && !back.on) {
				back.on = true;
				back = back.prev;
			}
		}
	}
	public void setNext(ConveyorSegment conveyorSegment) {
		next = conveyorSegment;
		conveyorSegment.prev = this;
	}
	public void setBox(Box b) {
		this.box = b;
		b.conveyor = this;
	}
}
