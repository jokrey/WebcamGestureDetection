package jokrey.experimental.ui;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public interface WebcamPanelListener {
	void imageChanged(BufferedImage newImage);
	void drawing(Graphics2D g);
	void initated();
	void rightClickedOn(MouseEvent me);
}