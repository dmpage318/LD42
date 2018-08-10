package TnT.ld.util;

import java.util.function.DoubleConsumer;

public class Looper {
	DoubleConsumer updateFunc;
	Thread thread;
	double frequency;
	
	public Looper(DoubleConsumer updateFunc, double frequencyHz) {
		this.updateFunc = updateFunc;
		frequency = frequencyHz;
	}
	
	public Looper start() {
		if (thread == null) {
			thread = new Thread(this::run);
			thread.start();
		}
		return this;
	}
	
	public Looper stop() {
		thread.interrupt();
		thread = null;
		return this;
	}
	
	public void run() {
		long lastStart = System.nanoTime();
		while (true) {
			long preTime = System.nanoTime();
			updateFunc.accept((preTime-lastStart)/1e9);
			lastStart = preTime;
			long postTime = System.nanoTime();
			long sleepTime = Math.max(0, (long) (1e9/frequency) + preTime - postTime);
			try {
				Thread.sleep(sleepTime/1000000, (int) (sleepTime%1000000));
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}
