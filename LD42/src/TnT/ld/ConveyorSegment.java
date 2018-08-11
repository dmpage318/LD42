package TnT.ld;

import java.awt.Color;
import java.awt.Graphics2D;

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
	public boolean permanentOn = false;
	public ConveyorSegment next;
	public static int BAR_WIDTH = 10;
	public int startDrawingX = 0;
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
		g.setColor(on ? Color.GREEN : Color.BLACK);
		g.drawRect(x, y, width, height);
		g.drawString("dx: "+dx+" dy: "+dy, x+20, y+20);
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
			if(dy > 0) {
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
		
		//NO DIAGONAL!
	}
}
