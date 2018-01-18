package jokrey.experimental.gesture_interpreter;

import java.awt.Graphics2D;
import java.awt.Point;

import jokrey.experimental.tracking.TrackingPointFinder;
import jokrey.experimental.ui.WebcamPanel;

public abstract class GestureInterpreter {
	protected Point oldTrackingPoint = null;
	public void updateTrackingPoint(Point trackingPoint, TrackingPointFinder trackPsFinder, WebcamPanel camP) {
		trackingPointLocationUpdated(trackingPoint, trackPsFinder, camP);
		oldTrackingPoint=trackingPoint;
	}
	public abstract String getName();
	protected abstract void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder, WebcamPanel camP);
	public abstract void drawStuff(Graphics2D g, WebcamPanel camP);

	public static GestureInterpreter getEmptyOne() {
		return new GestureInterpreter() {
			@Override public String getName(){return null;}
			@Override protected void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder,WebcamPanel camP){}
			@Override public void drawStuff(Graphics2D g,WebcamPanel camP){}
		};
	}
}