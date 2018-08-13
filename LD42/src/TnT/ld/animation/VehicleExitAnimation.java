package TnT.ld.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import TnT.ld.LD42;
import TnT.ld.Level;
import TnT.ld.Vehicle;

public class VehicleExitAnimation extends Animation {
	Level level;
	Vehicle vehicle;
	double xTime = 0.25;
	double accelTime, accelDist, cruiseTime;
	double extraDelay = 0.25;
	
	public VehicleExitAnimation(Level level) {
		this.level = level;
		vehicle = level.vehicle;
		double dist = LD42.width-level.vehicleX + 100;
		accelTime = Vehicle.speed/Vehicle.accel;
		accelDist = Vehicle.accel * accelTime * accelTime / 2;
		cruiseTime = (dist-accelDist)/Vehicle.speed;
	}

	public void initialize() {
		boolean full = true;
		f:for (int i = 0; i < vehicle.width; i++) {
			for (int j = 0; j < vehicle.height; j++) {
				if (!vehicle.filled[i][j]) {
					full = false;
					break f;
				}
			}
		}
		if (full) xTime = 0;
		level.shipmentInProgress = true;
	}
	
	public void update(double dt) {
		double t = elapsedTime();
		if ((t -= xTime) < 0) return;
		if (t < accelTime) {
			vehicle.x = (int) (level.vehicleX + Vehicle.accel * t * t / 2);
		} else {
			vehicle.x = (int) (level.vehicleX + accelDist + Vehicle.speed * (t-accelTime));
		}
	}

	public void paint(Graphics2D g) {
		double fill = Math.min(1, elapsedTime()/xTime);
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));
		int cs = level.cellSize;
		for (int i = 0; i < vehicle.width; i++) {
			for (int j = 0; j < vehicle.height; j++) {
				if (!vehicle.filled[i][j]) {
					int x = vehicle.x + i*cs;
					int y = vehicle.y + j*cs;
					for (int dx = 0; dx <= cs; dx += cs) {
						for (int dy = 0; dy <= cs; dy += cs) {
//							g.drawLine(x+dx, y+dy, (int) (x+dx+(cs/2-dx)*fill), y+dy);
//							g.drawLine(x+dx, y+dy, x+dx, (int) (y+dy+(cs/2-dy)*fill));
							g.drawLine(x+dx, y+dy, (int) (x+dx+(cs/2-dx)*fill), (int) (y+dy+(cs/2-dy)*fill));
						}
					}
				}
			}
		}
	}

	public boolean isFinished() {
		return elapsedTime() > xTime + accelTime + cruiseTime + extraDelay;
	}

	public void finish() {
		if (!level.gameOver) {
			level.shipIt();
		} else {
			level.showGameOver = true;
		}
	}
	
}
