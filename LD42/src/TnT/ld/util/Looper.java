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
			thread.setDaemon(true);
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
		long preact = 0;
		long lastStart = System.nanoTime();
		boolean first = true;
		while (true) {
			long preTime = System.nanoTime();
			if (!first) preact += ((long) (1e9/frequency) + lastStart - preTime)/10;
			updateFunc.accept((preTime-lastStart)/1e9);
			lastStart = preTime;
			long postTime = System.nanoTime();
			long sleepTime = Math.max(0, (long) (1e9/frequency) + preTime - postTime + preact);
			try {
				Thread.sleep(sleepTime/1000000, (int) (sleepTime%1000000));
			} catch (InterruptedException e) {
				return;
			}
			first = false;
		}
	}
	
}
