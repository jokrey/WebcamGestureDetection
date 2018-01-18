package jokrey.experimental.gesture_interpreter;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;

import jokrey.experimental.tracking.TrackingPointFinder;
import jokrey.experimental.ui.WebcamPanel;

public class Mouse_Controller_MovementBased extends GestureInterpreter {
	@Override public String getName(){return "Mouse_Controller_MovementBased";}
	private Robot r;

	final boolean invert_x_axis;
	final boolean invert_y_axis;
	public Mouse_Controller_MovementBased(boolean invert_x_axis, boolean invert_y_axis) {
		this.invert_x_axis=invert_x_axis;
		this.invert_y_axis=invert_y_axis;
		try {
			r=new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	@Override public void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder, WebcamPanel camP) {
		if(oldTrackingPoint!=null && newTrackingPoint!=null) {
			int x_change = oldTrackingPoint.x - newTrackingPoint.x;
			int y_change = oldTrackingPoint.y - newTrackingPoint.y;

			if(invert_x_axis)x_change= -x_change;
			if(invert_y_axis)y_change= -y_change;

			x_change*=4;
			y_change*=4;
//			System.out.println(x_change + " - " + y_change);
			if(Math.abs(x_change) < 250 && Math.abs(y_change) < 250) {
				Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();
				r.mouseMove(oldMouseLocation.x + x_change, oldMouseLocation.y + y_change);
			}
		}
	}
	@Override public void drawStuff(Graphics2D g, WebcamPanel campP) {}
}