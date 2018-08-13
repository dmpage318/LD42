package TnT.ld.animation;

import java.awt.Graphics2D;

import TnT.ld.LD42;
import TnT.ld.Level;
import TnT.ld.Vehicle;

public class VehicleEnterAnimation extends Animation {
	Level level;
	Vehicle vehicle;
	double accelTime, accelDist, cruiseTime;
	
	public VehicleEnterAnimation(Level level) {
		this.level = level;
		vehicle = level.vehicle;
		double dist = LD42.width-level.vehicleX;
		accelTime = Vehicle.speed/Vehicle.accel;
		accelDist = Vehicle.accel * accelTime * accelTime / 2;
		cruiseTime = (dist-accelDist)/Vehicle.speed;
	}

	public void initialize() {}
	public void update(double dt) {
		double t = accelTime + cruiseTime - elapsedTime();
		if (t < accelTime) {
			vehicle.x = (int) (level.vehicleX + Vehicle.accel * t * t / 2);
		} else {
			vehicle.x = (int) (level.vehicleX + accelDist + Vehicle.speed * (t-accelTime));
		}
	}

	public void paint(Graphics2D g) {}

	public boolean isFinished() {
		return elapsedTime() > accelTime + cruiseTime;
	}
	
	public void finish() {
		vehicle.x = level.vehicleX;
	}
	
}
