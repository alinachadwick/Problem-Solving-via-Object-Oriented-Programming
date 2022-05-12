import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 * @author Aspen Anderson and Alina Chadwick, Dartmouth CS10, January 2021
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * Problem Set 1
 */
public class RegionFinder {
	private static final int maxColorDiff = 20;              // how similar a pixel color must be to the target color, to belong to a region
	// suitable value for maxColorDiff depends on your implementation of colorMatch() and how much difference in color you want to allow

	private static final int minRegion = 50;                // how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions

	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private ArrayList<ArrayList<Point>> regions;            // a region is a list of points
	// so the identified regions are in a list of lists of points

	public RegionFinder() {
		this.image = null;
	}

	public RegionFinder(BufferedImage image) {
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	public BufferedImage visited;


	/**
	 * Sets regions to the flood fill regions in the image, similar enough to the targetColor.
	 */
	public void findRegions(Color targetColor) {
		// TODO: YOUR CODE HERE
		regions = new ArrayList<ArrayList<Point>>();   // new array list consisting of the array lists of regions
		visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {   // loops over all pixels in the image
				Color newColor = new Color(image.getRGB(x, y));   // newColor gets the color from the new image

				// if the color matches and the pixel has not been visited
				if (colorMatch(targetColor, newColor) && visited.getRGB(x, y) == 0) {

					ArrayList<Point> region = new ArrayList<Point>();
					ArrayList<Point> toVisit = new ArrayList<Point>();
					Point newPoint = new Point(x, y);
					toVisit.add(newPoint);   // added to the list to visit

					while (toVisit.size() != 0) {   // if the toVisit list is not empty
						Point currentPoint = toVisit.remove(toVisit.size() - 1);
						region.add(currentPoint);

						// visit current point and neighbor of current point
						visited.setRGB(currentPoint.x, currentPoint.y, 1);   // point is already visited

						// nested loop over neighbors and be careful not to go outside image (max, min stuff).
						ArrayList<Point> point = new ArrayList<>();
						for (int ny = Math.max(0, currentPoint.y - 1);
							 ny < Math.min(image.getHeight(), currentPoint.y + 2); ny++) {
							for (int nx = Math.max(0, currentPoint.x - 1);
								 nx < Math.min(image.getWidth(), currentPoint.x + 2); nx++) {

								// add all the neighbors (but not self) to the toVisit and region
								if (nx != currentPoint.x || ny != currentPoint.y) {
									Color c = new Color(image.getRGB(nx, ny));
									if (colorMatch(targetColor, c)) {
										if (visited.getRGB(nx, ny) == 0) {
											Point newestPoint = new Point(nx, ny);
											point.add(newestPoint);
											toVisit.add(newestPoint);
										}
									}
								}
							}
						}
					}
					// accounts for region size
					if (region.size() >= minRegion) {
						regions.add(region);
					}
				}
			}
		}
	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary)
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		// TODO: YOUR CODE HERE
		// uses absolute value method to check the difference in color
		if (Math.abs(c1.getRed() - c2.getRed()) < maxColorDiff) {
			if (Math.abs(c1.getGreen() - c2.getGreen()) < maxColorDiff) {
				if (Math.abs(c1.getBlue() - c2.getBlue()) < maxColorDiff) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		// TODO: YOUR CODE HERE
		// extracts the largest region based on the size method
		ArrayList largestRegion = new ArrayList<Point>();
		ArrayList max = regions.get(0);
		for (ArrayList<Point> i : regions) {
			if (i.size() > max.size()) {
				max = i;
				largestRegion.add(max);
			}
		}
		return max;
	}


	/**
	 * Sets recoloredImage to be a copy of image,
	 * but with each region a uniform random color,
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it

		// TODO: YOUR CODE HERE
		// sets a random color to the largest region
		for (ArrayList<Point> region : regions) {
			Color random = new Color ((int)(Math.random() * 255), (int)(Math.random() *255), (int)(Math.random()* 255));
			for (Point i: region){
				recoloredImage.setRGB(i.x, i.y, random.getRGB());
			}
		}
	}
}
