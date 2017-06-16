package gesture_interpreter;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;

import tracking.ColorTrackingPointFinder;
import tracking.MultiColorTracker;
import tracking.TrackingPointFinder;
import ui.WebcamPanel;
import util.swing.SWING_UTIL;

public class MultiTrackingInterpreter extends GestureInterpreter {
	@Override public String getName(){return "MultiTrackingInterpreter";}
	private Robot r;
	public MultiTrackingInterpreter() {
		try {
			r=new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	Rectangle screen = SWING_UTIL.getJoinedRectangle(SWING_UTIL.getRectangles(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()));
	private TrackingPointFinder trackPsFinder;
	@Override public void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder_g, WebcamPanel camP) {
		this.trackPsFinder=trackPsFinder_g;
		if(newTrackingPoint==null||camP.getIMG()==null)return;
		Point screenTranslatedPoint = new Point((int)((newTrackingPoint.getX()/camP.getIMG().getWidth())*screen.getWidth()), (int) ((newTrackingPoint.getY()/camP.getIMG().getHeight())*screen.getHeight()));
		screenTranslatedPoint.x=screen.width-screenTranslatedPoint.x;//to invert the mirrored view of the camera
		r.mouseMove(screenTranslatedPoint.x+screen.x, screenTranslatedPoint.y+screen.y);

		if(trackPsFinder instanceof MultiColorTracker) {
			Point redPoint = ((MultiColorTracker)trackPsFinder).trackers[0].getTrackingPoint();
			//Point greenPoint = ((MultiColorTracker)trackPsFinder).trackers[1].getTrackingPoint();
			Point bluePoint = ((MultiColorTracker)trackPsFinder).trackers[2].getTrackingPoint();
			if(redPoint!=null) {
				r.mouseRelease(MouseEvent.BUTTON1_MASK);
			} else if(bluePoint!=null) {
				r.mousePress(MouseEvent.BUTTON1_MASK);
			}
		}
	}
	@Override public void drawStuff(Graphics2D g, WebcamPanel camP) {
		if(trackPsFinder instanceof MultiColorTracker) {
			for(ColorTrackingPointFinder ctpf:((MultiColorTracker)trackPsFinder).trackers) {
				if(ctpf.getTrackingPoint()==null)continue;
				if(ctpf.currentTrackingColor.equals(Color.red))g.setColor(Color.blue);
				if(ctpf.currentTrackingColor.equals(Color.green))g.setColor(Color.orange);
				if(ctpf.currentTrackingColor.equals(Color.blue))g.setColor(Color.red);
				int diameter = 20;
				Point drawP = camP.getDrawPointForPOnImg(ctpf.getTrackingPoint());
				g.fillOval(drawP.x-diameter/2, drawP.y-diameter/2, diameter, diameter);
			}
		}
	}
}