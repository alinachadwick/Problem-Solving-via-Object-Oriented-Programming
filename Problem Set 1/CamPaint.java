import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Webcam-based drawing 
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 * @author Aspen Anderson and Alina Chadwick, Dartmouth CS10, January 2021
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 */
public class CamPaint extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private RegionFinder finder;			// handles the finding
	private Color targetColor=null;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
	private BufferedImage painting;			// the resulting masterpiece


	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaint() {
		finder = new RegionFinder();
		clearPainting();
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting, 
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {
		// TODO: YOUR CODE HERE
		if (displayMode == 'r') { // recolor
			g.drawImage(finder.getRecoloredImage(), 0, 0, null);
		}
		else if (displayMode == 'w') { // webcam
			g.drawImage(image, 0,0, null);
			super.draw(g);
		}
		else if (displayMode == 'p') { // painting
			g.drawImage(painting, 0, 0, null);
		}
	}

	/**
	 * Webcam method, here finding regions and updating painting.
	 */
	@Override
	public void processImage() {
		// TODO: YOUR CODE HERE
		if (image != null) {   // make sure the image is not empty
			finder.setImage(image);

			// sets the target color and recolors image
			if (targetColor != null) {
				finder.findRegions(targetColor);
				finder.recolorImage();
				finder.getRecoloredImage();

				// uses the largestRegion method to set the paint brush and use paint color
				if (finder.largestRegion() != null) {
					ArrayList<Point> brush = finder.largestRegion();
					for (Point pixel : brush) {
						painting.setRGB(pixel.x, pixel.y, paintColor.getRGB());
					}
				}
			}
		}
	}

	/**
	 * Overrides the DrawingGUI method to set targetColor.
	 */
	@Override
	public void handleMousePress(int x, int y) {
		if (image != null) { // to be safe, make sure webcam is grabbing an image
			// TODO: YOUR CODE HERE
			targetColor = new Color(image.getRGB(x, y));
			System.out.println("tracking " + targetColor);
		}
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { // clear
			clearPainting();
		}
		else if (k == 'o') { // save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { // save the painting
			saveImage(painting, "pictures/painting.png", "png");
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaint();
			}
		});
	}
}
