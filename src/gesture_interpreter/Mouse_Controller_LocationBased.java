package gesture_interpreter;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;

import tracking.TrackingPointFinder;
import ui.WebcamPanel;
import util.swing.SWING_UTIL;

public class Mouse_Controller_LocationBased extends GestureInterpreter {
	@Override public String getName(){return "Mouse_Controller_LocationBased";}
	private Robot r;
	public Mouse_Controller_LocationBased() {
		try {
			r=new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	Rectangle screen = SWING_UTIL.getJoinedRectangle(SWING_UTIL.getRectangles(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()));
	long lastBigChange = System.nanoTime();
	long lastClick = -1;
	Point lastBigChangePoint = new Point();
	final double positionLockAfter = 2;
	final double click_delay = 0.4;
	@Override public void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder, WebcamPanel camP) {
		if(newTrackingPoint==null||camP.getIMG()==null)return;
		Point screenTranslatedPoint = new Point((int)((newTrackingPoint.getX()/camP.getIMG().getWidth())*screen.getWidth()), (int) ((newTrackingPoint.getY()/camP.getIMG().getHeight())*screen.getHeight()));
		screenTranslatedPoint.x=screen.width-screenTranslatedPoint.x;//to invert the mirrored view of the camera
		r.mouseMove(screenTranslatedPoint.x+screen.x, screenTranslatedPoint.y+screen.y);

		double percentualChange_x = Math.abs(newTrackingPoint.getX()-lastBigChangePoint.getX())/camP.getIMG().getWidth();
		double percentualChange_y = Math.abs(newTrackingPoint.getY()-lastBigChangePoint.getY())/camP.getIMG().getHeight();

		if(percentualChange_x<0.01&&percentualChange_y<0.01) {
			if((System.nanoTime()-lastBigChange)/1e9 > positionLockAfter && (System.nanoTime()-lastClick)/1e9 >click_delay) {
				r.mousePress(MouseEvent.BUTTON1_MASK);
				r.mouseRelease(MouseEvent.BUTTON1_MASK);
				lastClick=System.nanoTime();
			}
		} else {
			lastBigChange=System.nanoTime();
			lastBigChangePoint=newTrackingPoint;
		}
	}
	@Override public void drawStuff(Graphics2D g, WebcamPanel camP) {
		if(oldTrackingPoint==null)return;
		g.setColor(Color.cyan);
		int diameter = 20;
		Point drawP = camP.getDrawPointForPOnImg(oldTrackingPoint);
		g.fillOval(drawP.x-diameter/2, drawP.y-diameter/2, diameter, diameter);
		g.setColor(Color.orange);
		int iameter = Math.min(20, (int) (20 * (((System.nanoTime()-lastBigChange)/1e9) / positionLockAfter)));
		g.fillOval(drawP.x-iameter/2, drawP.y-iameter/2, iameter, iameter);
	}
}