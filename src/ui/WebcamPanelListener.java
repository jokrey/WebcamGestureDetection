package ui;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public interface WebcamPanelListener {
	public void imageChanged(BufferedImage newImage);
	public void drawing(Graphics2D g);
	public void initated();
	void rightClickedOn(MouseEvent me);
}