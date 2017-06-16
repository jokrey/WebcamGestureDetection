package tracking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ui.WebcamPanel;
import util.UTIL_2D;

public abstract class TrackingPointFinder {
	public abstract Point getTrackingPoint();
	public abstract void update(BufferedImage img);
	public abstract void drawFindings(Graphics2D g, WebcamPanel camP);


	//STATIC HELPERS
	public static double getSimilarity(Color clr1, Color clr2) {
		return Math.sqrt(Math.pow(clr1.getRed()-clr2.getRed(),2) + Math.pow(clr1.getGreen()-clr2.getGreen(), 2) + Math.pow(clr1.getBlue()-clr2.getBlue(), 2));
	}
	public static boolean areSimilar(Color clr1, Color clr2, double det) {
		return getSimilarity(clr1, clr2) < det;
	}
	public static Point findClusterCenter(List<Point> clusterMatches) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for(Point p:clusterMatches) {
			if(p.x<minX)minX=p.x;
			if(p.x>maxX)maxX=p.x;
			if(p.y<minY)minY=p.y;
			if(p.y>maxY)maxY=p.y;
		}
		return new Point(minX+(maxX-minX)/2, minY+(maxY-minY)/2);
	}
	public static List<Point> removeFirstCluster(List<Point> colorMatches, int clusterMatchDiameter) {
		ArrayList<Point> matchesForCluster = new ArrayList<>();
		if(colorMatches.isEmpty())return matchesForCluster;
		ArrayList<Point> currentPointToCheck = new ArrayList<>();
		currentPointToCheck.add(colorMatches.remove(0));
		while(!colorMatches.isEmpty()) {
			matchesForCluster.addAll(currentPointToCheck);
			ArrayList<Point> nextPointToCheck = new ArrayList<>();
			for(int i=0;i<colorMatches.size();i++) {
				Point innerPoint = colorMatches.get(i);
				if(getSmallestDistanceTo(currentPointToCheck, innerPoint) <= clusterMatchDiameter) {
					nextPointToCheck.add(colorMatches.remove(i));
					i--;
				}
			}
			if(nextPointToCheck.isEmpty()) {
				return matchesForCluster;
			} else
				currentPointToCheck=nextPointToCheck;
		}
		return matchesForCluster;
	}
	public static double getSmallestDistanceTo(ArrayList<Point> ps, Point target) {
		double currentSmallest = Double.MAX_VALUE;
		for(Point p:ps) {
			double distance = getDistance(p, target);
			if(distance < currentSmallest)
				currentSmallest=distance;
		}
		return currentSmallest;
	}
	public static double getDistance(Point p, Point t) {
		return Math.sqrt(Math.pow(p.x-t.x, 2)+Math.pow(p.y-t.y, 2));
	}
	public static double pointToLineDistance(Point l1, Point l2, Point p) {
		double normalLength = Math.sqrt((l2.x-l1.x)*(l2.x-l1.x)+(l2.y-l1.y)*(l2.y-l1.y));
    	return Math.abs((p.x-l1.x)*(l2.y-l1.y)-(p.y-l1.y)*(l2.x-l1.x))/normalLength;
	}
	public double getDifferenceInPercent(double d1, double d2) {
		double difference = Math.abs(d1-d2);
		return difference/(d1/100);
	}

	public static int getLineAngle(Point[] line) {
		return UTIL_2D.getAngle(line[0], line[1]);
	}
	public static double getLineLength(Point[] line) {
		return getDistance(line[0], line[1]);
	}
	public static Point getLineMid(Point[] line) {
		return new Point((line[0].x+line[1].x)/2, (line[0].y+line[1].y)/2);
	}
	public static boolean doLinesIntersect(Point[] line1_ps, Point[] line2_ps) {
		Line2D line1 = new Line2D.Double(line1_ps[0], line1_ps[1]);
		Line2D line2 = new Line2D.Double(line2_ps[0], line2_ps[1]);
		return line1.intersectsLine(line2);
	}
	public static double getEasyDistance(Point[] line1_ps, Point[] line2_ps) {
		Line2D line1 = new Line2D.Double(line1_ps[0], line1_ps[1]);
		Line2D line2 = new Line2D.Double(line2_ps[0], line2_ps[1]);
		return
				Math.min(
						Math.min(line1.ptLineDist(line2.getP1()), line1.ptLineDist(line2.getP2()))
						,
						Math.min(line2.ptLineDist(line1.getP1()), line2.ptLineDist(line1.getP2()))
				);
	}

	//SLOWER BUT DOESN't remove stuff 
	public static List<Point> getFirstCluster(List<Point> colorMatches, int clusterMatchDiameter) {
		ArrayList<Point> matchesForCluster = new ArrayList<>();
		if(colorMatches.isEmpty())return matchesForCluster;
		Point currentPointToCheck = colorMatches.get(0);
		for(int outerI=0;outerI<colorMatches.size();outerI++) {
//		while(!colorMatches.isEmpty()) {
			matchesForCluster.add(currentPointToCheck);
			Point nextPointToCheck = null;
			for(int i=0;i<colorMatches.size();i++) {
				Point innerPoint = colorMatches.get(i);
				if(matchesForCluster.contains(innerPoint))continue;
				if(Math.sqrt(Math.pow(currentPointToCheck.x-innerPoint.x, 2)+Math.pow(currentPointToCheck.y-innerPoint.y, 2)) <= clusterMatchDiameter) {
					nextPointToCheck=innerPoint;
					break;
				}
			}
			if(nextPointToCheck==null) {
				return matchesForCluster;
//				currentPointToCheck = colorMatches.remove(0);
//				if(currentClusterMatches.size()>clusterSizeForMatch)
//					trackingPoints.add(findClusterCenter(currentClusterMatches));
//				currentClusterMatches.clear();
			} else
				currentPointToCheck=nextPointToCheck;
		}
		return matchesForCluster;
	}
}






//public List<Point> removeFirstCluster(List<Point> colorMatches, int clusterMatchDiameter) {
//	ArrayList<Point> matchesForCluster = new ArrayList<>();
//	if(colorMatches.isEmpty())return matchesForCluster;
//	Point currentPointToCheck = colorMatches.remove(0);
//	while(!colorMatches.isEmpty()) {
//		matchesForCluster.add(currentPointToCheck);
//		Point nextPointToCheck = null;
//		for(int i=0;i<colorMatches.size();i++) {
//			Point innerPoint = colorMatches.get(i);
//			if(Math.sqrt(Math.pow(currentPointToCheck.x-innerPoint.x, 2)+Math.pow(currentPointToCheck.y-innerPoint.y, 2)) <= clusterMatchDiameter) {
//				nextPointToCheck=colorMatches.remove(i);
//				break;
//			}
//		}
//		if(nextPointToCheck==null) {
//			return matchesForCluster;
////			currentPointToCheck = colorMatches.remove(0);
////			if(currentClusterMatches.size()>clusterSizeForMatch)
////				trackingPoints.add(findClusterCenter(currentClusterMatches));
////			currentClusterMatches.clear();
//		} else
//			currentPointToCheck=nextPointToCheck;
//	}
//	return matchesForCluster;
//}