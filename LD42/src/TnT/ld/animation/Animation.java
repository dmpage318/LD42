package TnT.ld.animation;

import java.awt.Graphics2D;

import TnT.ld.LD42;

public abstract class Animation {
	public abstract void initialize();
	public abstract void update(double dt);
	public abstract void paint(Graphics2D g);
	public abstract boolean isFinished();
	public abstract void finish();
	
	private boolean started;
	private boolean stopped;
	private long startTime;
	public void start() {
		if (started && !stopped) return;
		stopped = false;
		started = true;
		startTime = System.nanoTime();
		LD42.theLD.newAnimations.add(this);
	}
	
	public void stop() {
		stopped = true;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	public double elapsedTime() {
		return (System.nanoTime()-startTime)/1e9;
	}
}
