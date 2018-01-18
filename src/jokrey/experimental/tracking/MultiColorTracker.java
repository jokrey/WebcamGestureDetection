package jokrey.experimental.tracking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import jokrey.experimental.ui.WebcamPanel;

public class MultiColorTracker extends TrackingPointFinder {
	public final ColorTrackingPointFinder[] trackers = new ColorTrackingPointFinder[3];
	public MultiColorTracker() {
		trackers[0]=new ColorTrackingPointFinder(Color.red);
		trackers[1]=new ColorTrackingPointFinder(Color.green);
		trackers[2]=new ColorTrackingPointFinder(Color.blue);
	}
	@Override public Point getTrackingPoint() {
		for(ColorTrackingPointFinder tracker:trackers)
			if(tracker.getTrackingPoint()!=null)return tracker.getTrackingPoint();
		return null;
	}
	@Override public void update(BufferedImage img) {
		for(ColorTrackingPointFinder tracker:trackers)
			tracker.update(img);
	}
	@Override public void drawFindings(Graphics2D g, WebcamPanel camP) {}
}