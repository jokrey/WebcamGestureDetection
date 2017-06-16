package tracking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ui.WebcamPanel;

public class ColorTrackingPointFinder extends TrackingPointFinder {
	public ColorTrackingPointFinder(Color currentTrackingColor_g) {
		currentTrackingColor=currentTrackingColor_g;
	}

	@Override public Point getTrackingPoint() {
		return trackingPoints.isEmpty()?null:trackingPoints.get(0);
	}
	public List<Point> trackingPoints = new CopyOnWriteArrayList<>();
	public List<Point> colorMatches = new CopyOnWriteArrayList<>();

	public Color currentTrackingColor;//only works well with the rgb colors
	//change all these variables not just positiveColorMatchDeterminator!
	double positiveColorMatchDeterminator_max = 150;
	int clusterMatchDiameter = 5;
	int clusterSizeForMatch = 15;
	double positiveColorMatchDeterminator = positiveColorMatchDeterminator_max;
//	double positiveAbsoluteMatchDeterminator = 50;
	@Override public void update(BufferedImage img) {
		update(img, 0);
	}
//	private Point lastSingleTrackingPoint=null;
	public void update(BufferedImage img, int recursive) {
		List<Point> colorMatchesTemp = analyzeImageForColorMatches(img);
		colorMatches.clear();
		colorMatches.addAll(colorMatchesTemp);
		List<Point> trackingPointsTemp = analyzeMatchesForClusters(colorMatchesTemp);
		trackingPoints.clear();
		trackingPoints.addAll(trackingPointsTemp);

		if(recursive<=2) {//to prohibit endless loops
			if(trackingPoints.size()>1 &&
					positiveColorMatchDeterminator>5) {
				positiveColorMatchDeterminator-=5;
				System.out.println(positiveColorMatchDeterminator+"  -----");
				update(img, recursive+1);
			} else if(trackingPoints.isEmpty() &&
					positiveColorMatchDeterminator<positiveColorMatchDeterminator_max) {
				positiveColorMatchDeterminator+=5;
				System.out.println(positiveColorMatchDeterminator+"  ++++");
				update(img, recursive+1);
//				if(trackingPoints.size()==1)
			}
//			else if(trackingPoints.size()==1) {
//				lastSingleTrackingPoint = trackingPoints.get(0);
//			}
		}
	}
	public List<Point> analyzeImageForColorMatches(BufferedImage img) {
		List<Point> colorMatchesToRet = new ArrayList<>();
		for(int x=0;x<img.getWidth();x++) {
			for(int y=0;y<img.getHeight();y++) {
				Color clrAtP = new Color(img.getRGB(x, y));
				if(areSimilar(currentTrackingColor, clrAtP, positiveColorMatchDeterminator)
//						&&
//						Math.abs(currentTrackingColor.getRed() - clrAtP.getRed()) < positiveAbsoluteMatchDeterminator &&
//						Math.abs(currentTrackingColor.getGreen() - clrAtP.getGreen()) < positiveAbsoluteMatchDeterminator &&
//						Math.abs(currentTrackingColor.getBlue() - clrAtP.getBlue()) < positiveAbsoluteMatchDeterminator
						) {
					colorMatchesToRet.add(new Point(x,y));
				}
			}
		}
		return colorMatchesToRet;
	}
	public List<Point> analyzeMatchesForClusters(List<Point> colorMatchesTemp) {
		List<Point> trackingPointsToRet = new ArrayList<>();
		while(!colorMatchesTemp.isEmpty()) {
//		for(int i=0;i<colorMatches.size();) {
			List<Point> cluster = removeFirstCluster(colorMatchesTemp, clusterMatchDiameter);
			if(cluster.size()>clusterSizeForMatch)
				trackingPointsToRet.add(findClusterCenter(cluster));
//			i+=cluster.size();
		}
		return trackingPointsToRet;
	}


	@Override public void drawFindings(Graphics2D g, WebcamPanel camP) {
		if(!colorMatches.isEmpty()) {
			g.setColor(Color.white);
			int diameter = 6;
			for(Point match:colorMatches) {
				Point drawP = camP.getDrawPointForPOnImg(match);
				g.fillOval(drawP.x-diameter/2, drawP.y-diameter/2, diameter, diameter);
			}
		}
		g.setColor(Color.cyan);
		if(!trackingPoints.isEmpty()) {
			int trackingPointDiameter = 20;
			for(Point match:trackingPoints) {
				Point drawP = camP.getDrawPointForPOnImg(match);
				g.fillOval(drawP.x-trackingPointDiameter/2, drawP.y-trackingPointDiameter/2, trackingPointDiameter, trackingPointDiameter);
			}
		}
	}
}