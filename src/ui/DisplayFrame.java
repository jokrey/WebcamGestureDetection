package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import gesture_interpreter.GestureInterpreter;
import gesture_interpreter.Mouse_Controller_LocationBased;
import tracking.ColorTrackingPointFinder;
import tracking.TrackingPointFinder;
import util.swing.SWING_UTIL;
import util.swing.nicer.NicerFrame;

public class DisplayFrame extends NicerFrame {
	public static final void main(String[] args) {

//	GestureInterpreter gest_inter = GestureInterpreter.getEmptyOne();
//	GestureInterpreter gest_inter = new ActionDetection();
//	GestureInterpreter gest_inter = new PathDrawer();
//	GestureInterpreter gest_inter = new Mouse_Controller_MovementBased(false, true);
	GestureInterpreter gest_inter= new Mouse_Controller_LocationBased();
//	GestureInterpreter gest_inter = new MultiTrackingInterpreter();

//		TrackingPointFinder trackPsFinder = new MultiColorTracker();
		TrackingPointFinder trackPsFinder = new ColorTrackingPointFinder(Color.red);
//		TrackingPointFinder trackPsFinder = new ShapeTrackingPointsFinder();

		new DisplayFrame(trackPsFinder, gest_inter, true);
	}


    private boolean drawTrackingFindings;
    private boolean trackingPaused = false;

	private WebcamPanel webcamPanel;
	public DisplayFrame(final TrackingPointFinder trackPsFinder, final GestureInterpreter gest_inter, final boolean drawTrackingFindings_ingoing) {
		super(Color.DARK_GRAY, Color.cyan, true);

        drawTrackingFindings = drawTrackingFindings_ingoing;

		contentPane.setLayout(new BorderLayout());
		JPanel webcamDragAreaPanel = new JPanel(null) {
			@Override public Dimension getMinimumSize() {
				return new Dimension((int) (contentPane.getWidth()*0.75), contentPane.getHeight());
			}
			@Override public Dimension getMaximumSize() {
				return new Dimension((int) (contentPane.getWidth()*0.75), contentPane.getHeight());
			}
			@Override public Dimension getPreferredSize() {
				return new Dimension((int) (contentPane.getWidth()*0.75), contentPane.getHeight());
			}
		};
		contentPane.add(webcamDragAreaPanel, BorderLayout.WEST);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override public boolean dispatchKeyEvent(KeyEvent ke) {
//            	if(ke.getSource() instanceof JTextComponent/* || ke.getSource() instanceof NicerTextArea*/)return false;
                synchronized (DisplayFrame.class) {
                    int key = ke.getKeyCode();
                    switch (ke.getID()) {
	                    case KeyEvent.KEY_PRESSED:
//	                    	if (!pressedKeys.contains(key)) pressedKeys.add(key);
	                        break;
	                    case KeyEvent.KEY_RELEASED:
	                    	if(key==KeyEvent.VK_CONTROL) {
                                trackingPaused=!trackingPaused;
	                    	} else if(key==KeyEvent.VK_SPACE) {
	                    		drawTrackingFindings = !drawTrackingFindings;
	                    	} else if(key==KeyEvent.VK_ESCAPE) {
								webcamPanel.scaleToBiggestSize();
	                    	}
//	                    	try {
//	                    		pressedKeys.remove(pressedKeys.indexOf(key));
//	                    	} catch(Exception ex) {}
	                        break;
//	                    case KeyEvent.KEY_TYPED: break;      //never called for reasons
                    }
                    return false;
                }
            }
        });
		webcamPanel=new WebcamPanel(new WebcamPanelListener() {
			@Override public void imageChanged(BufferedImage newImage) {
				if(!trackingPaused)trackPsFinder.update(newImage);
				gest_inter.updateTrackingPoint(trackPsFinder.getTrackingPoint(), trackPsFinder, webcamPanel);
			}
			@Override public void drawing(Graphics2D g) {
//	        	g.drawImage(test, 0, 0, webcamPanel.getWidth(), webcamPanel.getHeight(), 0, 0, test.getWidth(), test.getHeight(), null);
//				g.setColor(Color.black);
//				g.fillRect(0, 0, webcamPanel.getWidth(), webcamPanel.getHeight());
				if(drawTrackingFindings)trackPsFinder.drawFindings(g, webcamPanel);
				gest_inter.drawStuff(g, webcamPanel);
//				if(!colorMatches.isEmpty()) {
//					g.setColor(Color.white);
//					int diameter = 6;
//					for(Point match:colorMatches) {
//						Point drawP = webcamPanel.getDrawPointForPOnImg(match);
//						g.fillOval((int)(drawP.x-diameter/2), (int)(drawP.y-diameter/2), diameter, diameter);
//					}
//				}
			}
			@Override public void rightClickedOn(MouseEvent me) {
//				if(trackPsFinder instanceof ColorTrackingPointFinder) {
//					Point pOnImg = webcamPanel.getPointOnImagetForPOnPanel(me.getPoint());
//					Color clrAtPoint = new Color(webcamPanel.getIMG().getRGB(pOnImg.x, pOnImg.y));
//					((ColorTrackingPointFinder)trackPsFinder).currentTrackingColor=clrAtPoint.brighter().brighter().brighter();
//					System.out.println(pOnImg);
//					System.out.println(clrAtPoint);
//					System.out.println(((ColorTrackingPointFinder)trackPsFinder).currentTrackingColor);
//				}
			}
			@Override public void initated() { 
				webcamPanel.scaleToBiggestSize();
			}
		});
//		contentPane.addMouseWheelListener(new MouseWheelListener() {
//			@Override public void mouseWheelMoved(MouseWheelEvent e) {
//				if(trackPsFinder instanceof ShapeTrackingPointsFinder) {
//					((ShapeTrackingPointsFinder) trackPsFinder).deter+=(1)*(e.getWheelRotation()<0?-1:1);
//					trackPsFinder.update(webcamPanel.getIMG());
//				}
//			}
//		});
		webcamDragAreaPanel.setBackground(Color.darkGray);
		webcamDragAreaPanel.add(webcamPanel);
		webcamDragAreaPanel.addComponentListener(new ComponentAdapter() {
			@Override public void componentShown(ComponentEvent e) {
				webcamPanel.scaleToBiggestSize();
			}
			@Override public void componentResized(ComponentEvent e) {
				webcamPanel.scaleToBiggestSize();
			}
		});
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed	(WindowEvent e) {closeEvnt();}
            @Override public void windowClosing	(WindowEvent e) {closeEvnt();}
            void closeEvnt(){
            	webcamPanel.dispose();
            	System.gc();
                System.exit(0);
            }
        });

        JPanel sideControlsP = new JPanel();
        sideControlsP.setBackground(Color.black);
		contentPane.add(sideControlsP, BorderLayout.CENTER);
		sideControlsP.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				webcamPanel.drawIMG=!webcamPanel.drawIMG;
				webcamPanel.repaint();
//				try {
//					ImageIO.write(webcamPanel.getIMG(), "PNG",new File("C:/Users/User/Desktop/testWebcamP.png"));
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
			}
		});
//		final BufferedImage test= ImageIO.read(new File("C:/Users/User/Desktop/testWebcamP.png"));
//		webcamPanel.holdIMG=true;
//		webcamPanel.cur_img=test;

		SWING_UTIL.resizeToGoodSize(this);
		SWING_UTIL.centerOnMouseScreen(this);
		setVisible(true);
	}
}