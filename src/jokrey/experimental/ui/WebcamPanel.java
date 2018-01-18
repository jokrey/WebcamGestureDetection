package jokrey.experimental.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;

import util.UTIL;
import util.swing.GrabCursorOnContainerClickEnabler;
import util.swing.MouseContainerDragEnabler;

public class WebcamPanel extends JPanel {
	public Webcam cam;
	public BufferedImage cur_img = null;
	public BufferedImage getIMG() {return cur_img;}
	public boolean holdIMG = false;
	public boolean drawIMG = true;
	public int captureDelay = 100;//4 FPS

	private WebcamPanelListener listener;
	public WebcamPanel(WebcamPanelListener listener_g) {
		this.listener=listener_g;
		Thread webcamT = new Thread(new Runnable() {
			@Override public void run() {
				cam=Webcam.getDefault();
				if(cam!=null) {
					if(!cam.isOpen()) {
						cam.setViewSize(cam.getViewSizes()[2]);
						cam.open();
					}
					if(!holdIMG)cur_img=cam.getImage();
				}
				listener.initated();
				while(true) {
					if(!holdIMG) {
						if(cam!=null&&cam.isOpen())
							cur_img=cam.getImage();
						else {
//							cur_img=null;//just keep the latest
						}
					}
					repaint();
					long before = System.nanoTime();
        			listener.imageChanged(cur_img);
        			long diffInMillis = (long) ((System.nanoTime()-before)/1e6);
        			long sleepTime = (long) (1000.0/cam.getFPS()) - diffInMillis;
        			if(sleepTime>5)//otherwise it will take longer to start the sleep and wake up than to simply not sleep -- tested: UTIL.sleep(1) takes up to 4 millis
        				UTIL.sleep(sleepTime);
				}
			}
		});
		webcamT.start();

		addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                try {
                	double oldZoomPercentage = zoomPercentage;
                	setZoomPercentage(e.getWheelRotation()<0?zoomPercentage+(zoomPercentage/10):zoomPercentage-(zoomPercentage/10));
                	if (getIMGPWidth()<10||getIMGPHeight()<10)
                		setZoomPercentage(oldZoomPercentage);
                    int oldRelativeLoc_x = (int) (e.getX()/(getWidth()/10000.0));
                    int oldRelativeLoc_y = (int) (e.getY()/(getHeight()/10000.0));
                    int newMouse_x = (int) (oldRelativeLoc_x*(getIMGPWidth()/10000.0));
                    int newMouse_y = (int) (oldRelativeLoc_y*(getIMGPHeight() /10000.0));
                    setLocation(getX()+(e.getX()-newMouse_x), getY()+(e.getY()-newMouse_y));
                    setSize(getIMGPWidth(),getIMGPHeight());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (!getBounds().intersects(getBounds()))
                    scaleToBiggestSize();
                repaint();
            }
        });
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					listener.rightClickedOn(e);
				} else if(SwingUtilities.isMiddleMouseButton(e)) {
					holdIMG=!holdIMG;
//					listener.middleClickedOn(e);
				}
			}
		});
        new MouseContainerDragEnabler(this, this, MouseContainerDragEnabler.KEEP_IN_BOUNDS_IF_POSSIBLE);
        new GrabCursorOnContainerClickEnabler(this, false);
        setBackground(Color.darkGray);
	}
    private double zoomPercentage = 100;
    private void setZoomPercentage(double zP) {
    	if (zP>=0.01)
    		zoomPercentage = zP;
    }
	void scaleToBiggestSize() {
		if(cam==null || cur_img==null)return;
		getParent().revalidate();
    	setZoomPercentage(100);
        while((getIMGPWidth()>getParent().getWidth() || getIMGPHeight()>getParent().getHeight()) && zoomPercentage>0.1)
        	setZoomPercentage(zoomPercentage-0.01);
        while((getIMGPWidth()<getParent().getWidth() && getIMGPHeight()<getParent().getHeight()))
        	setZoomPercentage(zoomPercentage+0.01);
        setSize(getIMGPWidth(), getIMGPHeight());
        setLocation(new Point(getParent().getWidth() / 2 - getWidth() / 2, getParent().getHeight() / 2 - getHeight() / 2));
        getParent().revalidate();
        revalidate();
        getParent().repaint();
        repaint();
	}
	private int getIMGPWidth() {
		return cur_img==null? 444 : (int) (cur_img.getWidth()*(zoomPercentage/100.0));
	}
	private int getIMGPHeight() {
		return cur_img==null? 444 : (int) (cur_img.getHeight()*(zoomPercentage/100.0));
	}
    @Override public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g =  (Graphics2D) gg;
        if(cur_img==null && isVisible()) {
			g.setColor(Color.GRAY);
			g.drawString("• • • • •", getWidth()/2-g.getFontMetrics().stringWidth("• • • • •")/2, getHeight()/2+g.getFontMetrics().getHeight()/4);
        } else {
        	if(drawIMG) {
	    		g.setComposite(AlphaComposite.Src);
	    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	//    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//A little higher quality, but about three times lower performance(which is visible)
	    		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        	g.drawImage(cur_img, 0, 0, getWidth(), getHeight(), 0, 0, cur_img.getWidth(), cur_img.getHeight(), null);
        	}
        	listener.drawing(g);
        }
    }

    public Point getDrawPointForPOnImg(Point pOnImg) {
    	return new Point((int)(pOnImg.x*(zoomPercentage/100.0)), (int)(pOnImg.y*(zoomPercentage/100.0)));
    }
	public Point getPointOnImagetForPOnPanel(Point point) {
    	return new Point((int)(point.x/(zoomPercentage/100.0)), (int)(point.y/(zoomPercentage/100.0)));
	}

	public void dispose() {
		cam.close();
	}
}