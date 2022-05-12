import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, explicit rectangle
 * @author CBK, Fall 2016, generic with Point2D interface
 * 
 */
public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// lower-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */
	public void insert(E p2) {
		// TODO: YOUR CODE HERE
		double x = point.getX();
		double y = point.getY();

		if (p2.getX()>x && p2.getX()<x2 && p2.getY()>y1 &&p2.getY()<y){ //quadrant check
			if (hasChild (1)){
				c1.insert(p2);
		}
			else{
			c1 =  new PointQuadtree<E>(p2, (int) x, y1, x2, (int) y);}
		}
		if (p2.getX()<x && p2.getX()>x1 && p2.getY()>y1 &&p2.getY()<y){ //quadrant check
			if (hasChild (2)){
				c2.insert(p2);
			}
			else{
				c2 = new PointQuadtree<E>(p2, x1, y1, (int) x, (int) y);}
		}
		if (p2.getX()<x && p2.getX()>x1 && p2.getY()>y &&p2.getY()<y2){ //quadrant check
			if (hasChild (3)){
				c3.insert(p2);
			}
			else{
				c3 = new PointQuadtree<E>(p2, x1, (int) y, (int) x, y2);}
		}
		if (p2.getX()>x && p2.getX()<x2 && p2.getY()>y &&p2.getY()<y2){ //quadrant check
			if (hasChild (4)){
				c4.insert(p2);
			}
			else{
				c4 = new PointQuadtree<E>(p2, (int) x, (int) y, x2, y2);}
		}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		// TODO: YOUR CODE HERE -- compute directly, using only numbers not lists (i.e., don't just call allPoints() and return its size)
        //recursive size algorithm, calling recursively on children
		int num = 1;
		if (hasChild(1)){
			num += c1.size();
		}
		if (hasChild(2)){
			num += c2.size();
		}
		if (hasChild(3)){
			num += c3.size();
		}
		if (hasChild(4)){
			num += c4.size();
		}
	return num;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {
		// TODO: YOUR CODE HERE -- efficiency matters!
		List<E> a = new ArrayList<E>();
		addToAllPoints(a);          //calls helper function
		return a;
	}	

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		// TODO: YOUR CODE HERE -- efficiency matters!
		List<E> f = new ArrayList<E>();
		accumulator(f, cx, cy, cr);     //calls helper method
		return f;

	}
    // TODO: YOUR CODE HERE for any helper methods
	//recursive helper method for findInCircle
	public void accumulator(List<E> findInCircle, double cx, double cy, double cr){
		//To find all points within the circle (cx,cy,cr), stored in a tree covering rectangle (x1,y1)-(x2,y2)
		//If the circle intersects the rectangle
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {
			//If the tree's point is in the circle, then the blob is a "hit"
			if (Geometry.pointInCircle(point.getX(), point.getY(), cx, cy, cr)) {
				findInCircle.add(point);
			}
				//For each quadrant with a child, recurse with that child
				if (hasChild(1)) {
					c1.accumulator(findInCircle, cx, cy, cr);
				}
				if (hasChild(2)) {
					c2.accumulator(findInCircle, cx, cy, cr);
				}
				if (hasChild(3)) {
					c3.accumulator(findInCircle, cx, cy, cr);
				}
				if (hasChild(4)) {
					c4.accumulator(findInCircle, cx, cy, cr);
				}
			}
	else{
		return;
		}

	}


    //helper function for the allPoints method, recursively calls children
	public void addToAllPoints(List<E> allPoints){
		allPoints.add(point);       //adds to list
		if (hasChild(1)){
			c1.addToAllPoints(allPoints);
		}
		if (hasChild(2)){
			c2.addToAllPoints(allPoints);
		}
		if (hasChild(3)){
			c3.addToAllPoints(allPoints);
		}
		if (hasChild(4)){
			c4.addToAllPoints(allPoints);
		}
	}
	public String toString() {
		return toStringHelper("");}     //calls helper method

    //helper method for toString
    //test out tree and print it out
	public String toStringHelper(String indent){
		String res = indent + point + "\n";
		if (hasChild(1)){
			res += c1.toStringHelper(indent + " ");}
		if (hasChild(2)){
			res += c2.toStringHelper(indent + " ");}
		if(hasChild(3)){
			res += c3.toStringHelper(indent + " ");}
		if(hasChild(4)){
			res += c4.toStringHelper(indent + " ");}
		return res;
	}
}
