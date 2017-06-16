package tracking;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ui.WebcamPanel;
import util.UTIL;

public class ShapeTrackingPointsFinder extends TrackingPointFinder {
	@Override public Point getTrackingPoint() {
		return null;
	}
	public List<Point> shapeOutlinePoints = new CopyOnWriteArrayList<>();
	public List<Point[]> linePoints = new CopyOnWriteArrayList<>();
	public List<Point> trackingPoints = new CopyOnWriteArrayList<>();
	public List<List<Point[]>> similarLines = new CopyOnWriteArrayList<>();

	public double deter = 80;
	@Override public void update(BufferedImage img) {
//		System.out.println(deter);
		List<Point> shapeOutlinePointsTemp=analyzeImageForShapeOutlines(img);
		shapeOutlinePoints.clear();
		shapeOutlinePoints.addAll(shapeOutlinePointsTemp);
		List<Point[]> lines=analyzePointsForLines(shapeOutlinePointsTemp);
		linePoints.clear();
		linePoints.addAll(lines);
		List<Point> trackingPointsTemp=analyzeLinesForHand(lines);
		trackingPoints.clear();
		trackingPoints.addAll(trackingPointsTemp);
		similarLines.clear();
		similarLines.addAll(clusterLinesWithSizeBiggerThan(filterLinesThatIntersect(clusterLinesWithSimilarDistance(clusterLinesWithSimilarAngle(analyzePointsForLines(analyzeImageForShapeOutlines(img)), 30), 100)), 3));
	}
	public List<Point> analyzeImageForShapeOutlines(BufferedImage img) {
		List<Point> points = new ArrayList<>();
		for(int x=0;x<img.getWidth()-1;x++) {
			for(int y=0;y<img.getHeight()-1;y++) {
				Color clrAtXY = new Color(img.getRGB(x, y));
				Color clrAtXP1 = new Color(img.getRGB(x+1, y));
				Color clrAtYP1 = new Color(img.getRGB(x, y+1));
				if(getSimilarity(clrAtXY, clrAtXP1) > deter || getSimilarity(clrAtXY, clrAtYP1) > deter) {
					points.add(new Point(x,y));
				}
			}
		}
		return points;
	}
	public static List<Point[]> analyzePointsForLines(List<Point> shapeOutlinePoints) {
		List<Point[]> linePoints = new ArrayList<>();
		double absolutMinShapeSize = 40;

		List<List<Point>> clusters = new ArrayList<>();
		while(!shapeOutlinePoints.isEmpty()) {
			List<Point> cluster = removeFirstCluster(shapeOutlinePoints, 3);
			clusters.add(cluster);
		}

		while(!clusters.isEmpty()) {
			List<Point> currentCluster = clusters.remove(0);
			List<Point> removedFromCurCluster = new ArrayList<>();
			int origCurClusterSize = currentCluster.size();

			while(!currentCluster.isEmpty()) {
				Point p0 = currentCluster.get(0);
				Point pEnd = currentCluster.get(currentCluster.size()-1);

//				double averagePointToLineDistanceOfNewCluster = 0;
				List<Point> newCluster = new ArrayList<>();
				for(Point p:currentCluster) {
					if(pointToLineDistance(p0, pEnd, p) < 4 && (newCluster.isEmpty() || getDistance(newCluster.get(newCluster.size()-1), p) < 25)) {
//						averagePointToLineDistanceOfNewCluster+=pointToLineDistance(p0, pEnd, p);
						newCluster.add(p);
					}
				}
				if(newCluster.size()>absolutMinShapeSize) {
//					System.out.println(origCurClusterSize+" > "+newCluster.size()+"   "+averagePointToLineDistanceOfNewCluster/newCluster.size());
					linePoints.add(new Point[] {newCluster.get(0), newCluster.get(newCluster.size()-1)});
					currentCluster.removeAll(newCluster);
				} else {
					for(int i=0;i<6&&i<currentCluster.size();i++)
						removedFromCurCluster.add(currentCluster.remove(i));
				}
			}
			if(!removedFromCurCluster.isEmpty() && removedFromCurCluster.size()<origCurClusterSize)clusters.add(removedFromCurCluster);
		}
//		System.out.println();
		return linePoints;
	}
	public List<Point> analyzeLinesForHand(List<Point[]> lines) {
		ArrayList<Point[]> origLines = new ArrayList<>();
		origLines.addAll(lines);

		ArrayList<Point> handMidPoints = new ArrayList<>();

//		System.out.println("Printing line stats for testing:::::::");
//		System.out.println();
//		for(Point[] line:lines) {
//			printLineStats(line); 
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println();

		ArrayList<ArrayList<Point[]>> lineClustersWithSimilarAngle = clusterLinesWithSimilarAngle(lines, 30);
		for(ArrayList<Point[]> lineCluster:lineClustersWithSimilarAngle) System.out.println("similars(angle): "+lineCluster.size());
		List<List<Point[]>> lineClustersWithSimilarDistance = clusterLinesWithSimilarDistance(lineClustersWithSimilarAngle, 100);
		for(List<Point[]> lineCluster:lineClustersWithSimilarDistance) System.out.println("similars(distance): "+lineCluster.size());
		List<List<Point[]>> lineClustersWithoutIntersectors = filterLinesThatIntersect(lineClustersWithSimilarDistance);
		for(List<Point[]> lineCluster:lineClustersWithoutIntersectors) System.out.println("similars(no inter): "+lineCluster.size());
		List<List<Point[]>> lineClustersWithSize = clusterLinesWithSizeBiggerThan(lineClustersWithoutIntersectors, 3);
		for(List<Point[]> lineCluster:lineClustersWithSize) System.out.println("similars(size): "+lineCluster.size());

		for(List<Point[]> lineCluster:lineClustersWithSize) {
			boolean anyIntersectAny = false;
			for(int i=0;i<lineCluster.size();i++) {
				Point[] origLine = lineCluster.get(i);
				Point[] lengthenedLine = lengthenLine(origLine, 0.65);

				if(intersectsAnyOf(lengthenedLine, origLines)) {
					anyIntersectAny = true;
					break;
				}
			}
			if(!anyIntersectAny) {
				ArrayList<Point> clustered = new ArrayList<>();
				for(Point[] pa:lineCluster){
					clustered.add(pa[0]);
					clustered.add(pa[1]);
				}
				handMidPoints.add(findClusterCenter(clustered));
//				handMidPoints.add(lengthenLine(lineCluster.get(1), 0.8)[1]);
			}
		}

		return handMidPoints;
	}
	public Point[] lengthenLine(Point[] line, double by) {
//		Point p0=line[0];
//		double angle = Math.atan2(line[1].y - line[0].y, line[1].x - line[0].x);
//		double oldLength = getLineLength(line);
//		double newLength = oldLength+oldLength*by;
//		Point pEnd = new Point(
//			(int)(p0.x + newLength * Math.cos(angle))
//			,
//			(int)(p0.y + newLength * Math.cos(angle))
//		);
//		return new Point[] {p0, pEnd};
		Point p0 = line[0];
		Point pEndOld = line[1];
		double oldLength = getLineLength(line);
		double newLength = oldLength+oldLength*by;
		double ratio = newLength/oldLength;//or the other way around try!
		Point pEnd = new Point(
			(int)((1-ratio)*p0.x + ratio*pEndOld.x)
			,
			(int)((1-ratio)*p0.y + ratio*pEndOld.y)
		);
		return new Point[] {p0, pEnd};
	}
	public ArrayList<ArrayList<Point[]>> clusterLinesWithSimilarAngle(List<Point[]> origLines, double deviation) {
		ArrayList<ArrayList<Point[]>> lineClusters = new ArrayList<>();
		while(!origLines.isEmpty()) {
			ArrayList<Point[]> similarAngleCluster = new ArrayList<>();
			similarAngleCluster.add(origLines.remove(0));
			int firstAngle = getLineAngle(similarAngleCluster.get(0));
			for(int i=0;i<origLines.size();i++) {
				if(Math.abs(firstAngle - getLineAngle(origLines.get(i))) < deviation) {
					similarAngleCluster.add(origLines.remove(i));
					i--;
				}
			}
			lineClusters.add(similarAngleCluster);
		}
		return lineClusters;
	}
//	public ArrayList<ArrayList<Point[]>> getLinesWithSimilarLength(ArrayList<ArrayList<Point[]>> origLineClusters, double deviationInPercent) {
//		ArrayList<ArrayList<Point[]>> lineClusters = new ArrayList<>();
//		while(!origLineClusters.isEmpty()) {
//			ArrayList<Point[]> similarLengthCluster = new ArrayList<>();
//			similarLengthCluster.addAll(origLineClusters.remove(0));
//			double firstLength = getLineLength(similarLengthCluster.get(0));
//			Iterator<Point[]> similarIterator = similarLengthCluster.iterator();
//			while(similarIterator.hasNext()) {
//				Point[] line = similarIterator.next();
//				System.out.println(getDifferenceInPercent(firstLength, getLineLength(line)));
//				if(getDifferenceInPercent(firstLength, getLineLength(line)) > deviationInPercent) {
//					similarIterator.remove();
//				}
//			}
//			lineClusters.add(similarLengthCluster);
//		}
//		return lineClusters;
//	}
	public List<List<Point[]>> clusterLinesWithSimilarDistance(ArrayList<ArrayList<Point[]>> origLineClusters, double distance) {
		List<List<Point[]>> newClusters = new ArrayList<>();
		while(!origLineClusters.isEmpty()) {
			ArrayList<Point[]> cluster = origLineClusters.remove(0);
			ArrayList<Point[]> newCluster = new ArrayList<>();
//			double averageDistance = getAverageLineDistance(cluster);
			Iterator<Point[]> clusterIter = cluster.iterator();
			while(clusterIter.hasNext()) {
				Point[] line = clusterIter.next();
				if(previousLineDistance(line, cluster) > distance) {
					clusterIter.remove();
					newCluster.add(line);
				}
			}
			if(!cluster.isEmpty())newClusters.add(cluster);
			if(!newCluster.isEmpty())origLineClusters.add(newCluster);
		}
		return newClusters;
	}
	private List<List<Point[]>> clusterLinesWithSizeBiggerThan(List<List<Point[]>> lineClusters, int size) {
		Iterator<List<Point[]>> lineClustersIter = lineClusters.iterator();
		while(lineClustersIter.hasNext()) {
			List<Point[]> cluster = lineClustersIter.next();
			if(cluster.size()<size) lineClustersIter.remove();
		}
		return lineClusters;
	}
	private List<List<Point[]>> filterLinesThatIntersect(List<List<Point[]>> lineClusters) {
		Iterator<List<Point[]>> lineClustersIter = lineClusters.iterator();
		while(lineClustersIter.hasNext()) {
			List<Point[]> cluster = lineClustersIter.next();
			Iterator<Point[]> clusterIter = cluster.iterator();
			while(clusterIter.hasNext()) {
				Point[] line = clusterIter.next();
				if(intersectsAnyOf(line, cluster))
					clusterIter.remove();
			}
			if(cluster.isEmpty()) lineClustersIter.remove();
		}
		return lineClusters;
	}

	public static double previousLineDistance(Point[] line, ArrayList<Point[]> lines) {
		for(int i=0;i<lines.size();i++) {
			if(line[0].equals(lines.get(i)[0]) && line[1].equals(lines.get(i)[1]))
				return getEasyDistance(line, lines.get(UTIL.returnIntoBounds(i-1, lines.size())));
//			if( && smallestDistance > getEasyDistance(line, curLine))
//				smallestDistance=getEasyDistance(line, curLine);
		}
		return Double.MAX_VALUE;
	}
	public static double closestLineDistance(Point[] line, ArrayList<Point[]> lines) {
		double smallestDistance = Double.MAX_VALUE;
		for(int i=0;i<lines.size();i++) {
			Point[] curLine = lines.get(i);
			if(!line[0].equals(curLine[0]) && !line[1].equals(curLine[1]) && smallestDistance > getEasyDistance(line, curLine))
				smallestDistance=getEasyDistance(line, curLine);
		}
		return smallestDistance;
	}
	public static double getAverageLineDistance(ArrayList<Point[]> lines) {
		double addedDistance = 0;
		for(int i=0;i+1<lines.size();i++)
			addedDistance+=getEasyDistance(lines.get(i), lines.get(i+1));
		return addedDistance/lines.size();
	}
	public static boolean intersectsAnyOf(Point[] line, List<Point[]> cluster) {
		for(Point[] cluster_line:cluster) {
			if(!line[0].equals(cluster_line[0]) && !line[1].equals(cluster_line[1]) && doLinesIntersect(line, cluster_line)) {
				return true;
			}
		}
		return false;
	}


	public void printLineStats(Point[] line) {
		System.out.println("Line:");
		System.out.println("P1:"+line[0]);
		System.out.println("P2:"+line[1]);
		System.out.println("Angle: "+getLineAngle(line));
		System.out.println("Length: "+getLineLength(line));
		System.out.println("Mid: "+getLineMid(line));
		System.out.println();
	}

	@Override public void drawFindings(Graphics2D g, WebcamPanel camP) {
		g.setColor(Color.red);
		int diameter = 4;
		for(Point match:shapeOutlinePoints) {
			Point drawP = camP.getDrawPointForPOnImg(match);
			g.fillOval(drawP.x-diameter/2, drawP.y-diameter/2, diameter, diameter);
		}

		int lineEndDiameter = 10;
		g.setStroke(new BasicStroke(4f));
		for(int i=0;i<linePoints.size();i++) {
			Point drawP = camP.getDrawPointForPOnImg(linePoints.get(i)[0]);
			Point drawP2 = camP.getDrawPointForPOnImg(linePoints.get(i)[1]);
			g.setColor(Color.cyan);
			g.drawLine(drawP.x, drawP.y, drawP2.x, drawP2.y);
			g.setColor(Color.YELLOW);
			g.fillOval(drawP.x-lineEndDiameter/2, drawP.y-lineEndDiameter/2, lineEndDiameter, lineEndDiameter);
			g.fillOval(drawP2.x-lineEndDiameter/2, drawP2.y-lineEndDiameter/2, lineEndDiameter, lineEndDiameter);
		}

		int trackingPDia = 50;
		for(Point p:trackingPoints) {
			Point drawP = camP.getDrawPointForPOnImg(p);
			g.setColor(Color.RED);
			g.fillOval(drawP.x-trackingPDia/2, drawP.y-trackingPDia/2, trackingPDia, trackingPDia);
		}

//		g.setStroke(new BasicStroke(4f));
//		for(int outI=0;outI<similarLines.size();outI++) {
//			List<Point[]> similarLines_b = similarLines.get(outI);
//			System.out.println(similarLines_b.size());
//			g.setColor(getDistinctColorFor(outI));
//			for(int i=0;i<similarLines_b.size();i++) {
//				Point[] origLine = similarLines_b.get(i);
//				Point drawP = camP.getDrawPointForPOnImg(origLine[0]);
//				Point drawP2 = camP.getDrawPointForPOnImg(origLine[1]);
//				g.drawLine(drawP.x, drawP.y, drawP2.x, drawP2.y);
////				g.fillOval((int)(drawP.x-lineEndDiameter/2), (int)(drawP.y-lineEndDiameter/2), lineEndDiameter, lineEndDiameter);
////				g.fillOval((int)(drawP2.x-lineEndDiameter/2), (int)(drawP2.y-lineEndDiameter/2), lineEndDiameter, lineEndDiameter);
//
//				Point[] lengthenedLine = lengthenLine(origLine, 0.75);
//				drawP = camP.getDrawPointForPOnImg(lengthenedLine[0]);
//				drawP2 = camP.getDrawPointForPOnImg(lengthenedLine[1]);
//				g.drawLine(drawP.x, drawP.y, drawP2.x, drawP2.y);
//				
//			}
//		}
//		System.out.println();
//		System.out.println();
	}
	public Color getDistinctColorFor(int i) {
		if(i==0) {
			return Color.BLUE;
		} else if(i==1) {
			return Color.CYAN;
		} else if(i==2) {
			return Color.GREEN;
		} else if(i==3) {
			return Color.MAGENTA.darker().darker();
		} else if(i==4) {
			return Color.ORANGE;
		} else if(i==5) {
			return Color.PINK.darker();
		} else if(i==6) {
			return Color.RED.brighter();
		} else if(i==7) {
			return Color.YELLOW;
		}
		return Color.BLACK;
	}
}





























//BACKUP

//public void analyzePointsForShapes(List<Point> shapeOutlinePoints) {
//	trackingPoints.clear();
//	if(shapeOutlinePoints.isEmpty())return;
//	Point currentPointToCheck = shapeOutlinePoints.remove(0);
//	ArrayList<Point> currentPointsInLine = new ArrayList<>();
////	int lineMatchDiameter = 3;
////	int firstAngleInLine = -1;
//	int linePAmountForMatch = 60;
//	while(!shapeOutlinePoints.isEmpty()) {
//		currentPointsInLine.add(currentPointToCheck);
//		Point nextPointToCheck = null;
//		for(int i=0;i<shapeOutlinePoints.size();i++) {
//			Point innerPoint = shapeOutlinePoints.get(i);
//			double distance = Math.sqrt(Math.pow(currentPointToCheck.x-innerPoint.x, 2)+Math.pow(currentPointToCheck.y-innerPoint.y, 2));
//			if(distance <= 2) {
//				nextPointToCheck=shapeOutlinePoints.remove(i);
//			}
////			if(distance > 1 && distance < 5) {
////				nextPointToCheck=shapeOutlinePoints.get(i);
////				int angle = SWING_UTIL.getAngle(currentPointToCheck, nextPointToCheck);
//////				System.out.println(distance + " - ("+firstAngleInLine+")("+angle+") "+Math.min(Math.abs(firstAngleInLine - angle), 360-Math.abs(firstAngleInLine - angle)));
////				if(firstAngleInLine == -1 || Math.min(Math.abs(firstAngleInLine - angle), 360-Math.abs(firstAngleInLine - angle)) < 80) {
////					if(firstAngleInLine==-1)firstAngleInLine=angle;
////					shapeOutlinePoints.remove(i);
////					break;
////				} else
////					shapeOutlinePoints.remove(i);
////			} else if(distance <= 1) {
////				shapeOutlinePoints.remove(i);
//////				break;
////			}
//		}
//		if(nextPointToCheck==null) {
//			if(currentPointsInLine.size()>linePAmountForMatch)
//				trackingPoints.add(findClusterCenter(currentPointsInLine));
//			currentPointsInLine.clear();
//			if(shapeOutlinePoints.isEmpty())break;
//			currentPointToCheck = shapeOutlinePoints.remove(0);
////			firstAngleInLine = -1;
//		} else
//			currentPointToCheck=nextPointToCheck;
//	}
//	if(currentPointsInLine.size()>linePAmountForMatch) {
//		trackingPoints.add(findClusterCenter(currentPointsInLine));
//	}
//}