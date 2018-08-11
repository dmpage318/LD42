package TnT.ld.animation;

import java.awt.Graphics2D;
import java.util.List;

import TnT.ld.ConveyorSegment;

public class ConveyorAnimator extends Animation {
	List<ConveyorSegment> segs;
	private double last = 0;
	public ConveyorAnimator(List<ConveyorSegment> segs) {
		this.segs = segs;
	}
	@Override
	public void initialize() {
	}

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		if(elapsedTime() - last < .01) {
			return;
		}
		for(ConveyorSegment s : segs) {
			if(s.on) {
				s.startDrawingX -= 1; 
				if(s.startDrawingX < -2 *ConveyorSegment.BAR_WIDTH) {
					s.startDrawingX = 0;
				}
			}
		}
		last = elapsedTime();
	}

	@Override
	public void paint(Graphics2D g) {
		//Nothing to do, handled by conveyor seg
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void finish() {

	}

}
