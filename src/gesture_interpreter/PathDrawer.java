package gesture_interpreter;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import tracking.TrackingPointFinder;
import ui.WebcamPanel;

public class PathDrawer extends GestureInterpreter {
	@Override public String getName(){return "PathDrawer";}
	ArrayList<Point> path = new ArrayList<>();

	long lastTimeTrackingPointWasFound = 0;
	@Override public void trackingPointLocationUpdated(Point newTrackingPoint, TrackingPointFinder trackPsFinder, WebcamPanel camP) {
		if(newTrackingPoint!=null) {
			lastTimeTrackingPointWasFound=System.nanoTime();
			Point lastExistingOldTrackingPoint = path.isEmpty()?null:path.get(path.size()-1);

			double distance = lastExistingOldTrackingPoint==null?-1:Math.sqrt(Math.pow(newTrackingPoint.x-lastExistingOldTrackingPoint.x, 2) + Math.pow(newTrackingPoint.y-lastExistingOldTrackingPoint.y, 2));
			System.out.println("distance: "+distance);
			if(lastExistingOldTrackingPoint==null || (distance > 5 && distance < 55)) {  //!path.contains(newTrackingPoint)
				System.out.println("add");
				path.add(newTrackingPoint);
			}
//			else if(distance > 250)path.clear();
		} else {
			if(lastTimeTrackingPointWasFound == 0 || (System.nanoTime()-lastTimeTrackingPointWasFound)/1e9 > 0.8)
				path.clear();
		}
//		else {
//			path.clear();
//		}
	}
	@Override public void drawStuff(Graphics2D g, WebcamPanel camP) {
		g.setStroke(new BasicStroke(4f));
		for(int i=0;i<path.size()-1;i++) {
			Point p1 = camP.getDrawPointForPOnImg(path.get(i));
			Point p2 = camP.getDrawPointForPOnImg(path.get(i+1));
			
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}
}