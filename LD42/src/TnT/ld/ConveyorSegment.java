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
	public ConveyorSegment(int x, int y, int dx, int dy) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);
		g.drawString("dx: "+dx+" dy: "+dy, x+20, y+20);
	}
}
