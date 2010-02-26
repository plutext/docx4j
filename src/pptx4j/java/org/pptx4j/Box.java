package org.pptx4j;

public class Box {
	
	public Box(long offsetX, long offsetY, long extentX, long extentY) {
		offset = new Point(offsetX, offsetY);
		extent = new Point(extentX, extentY);
		
		System.out.println(debug());
	}
	
	// Position of top left corner,
	// from origin in top lef
	Point offset;

	// Length of sides
	Point extent;

	/**
	 * Flip across Y axis
	 */
	public void flipH() {
		offset.x = offset.x + extent.x;
		extent.x = -extent.x;		

		System.out.println("FlipH--> " + debug());
	}
	
	/**
	 * Flip across X axis, so Y co-ordinate changes
	 */
	public void flipV() {		
		offset.y = offset.y + extent.y;
		extent.y = -extent.y;

		System.out.println("FlipV--> " + debug());
	}

	public Point getOtherCorner() {
		return offset.add(extent);
	}
	
	public void toPixels() {
		offset = offset.toPixels();
		extent = extent.toPixels();
	}
	
	/**
	 * Clockwise rotation, about the bounding-box centre
	 * 
	 * @param units  are 60 thousandths of a degree
	 */
	public void rotate(int units) {
		
		// For translation
		Point centre = new Point(
						Math.round(offset.x + 0.5f* extent.x),
						Math.round(offset.y + 0.5f* extent.y)) ;
		
		float degree = -units/60000; // clockwise
		System.out.println("Rotating " + degree);
		float radians = (float)Math.toRadians(degree);

		// Extent
		// .. work out 
		Point otherCorner      = getOtherCorner();
		Point otherCornerDash  = otherCorner.subtract(centre);
		Point otherCornerDash2 = rotate(otherCornerDash, radians);
		Point otherCornerDash3 = otherCornerDash2.add(centre);
		
		// Offset
		Point offsetDash = offset.subtract(centre);
		Point offsetDash2 = rotate(offsetDash, radians);
		Point offsetDash3 = offsetDash2.add(centre);	
		
		offset = offsetDash3;
		extent = otherCornerDash3.subtract(offset);
		
		System.out.println("Rotated--> " + debug());
		
	}
	
	
	private Point rotate(Point p, float radians) {
		
		long xDash = Math.round((p.x*Math.cos(radians)) -(p.y*Math.sin(radians)));		
		long yDash = Math.round((p.x*Math.sin(radians)) + (p.y*Math.cos(radians)));
		
		return new Point(xDash, yDash);
		
	}
	
	public String debug() {
		return "offset " + offset.debug() + "; extent " + extent.debug();
	}
	
	
	public Point getOffset() {
		return offset;
	}

	public Point getExtent() {
		return extent;
	}
//	public String getExtentYAsString() {
//		return Long.toString(extentY);
//	}

	public static void main(String[] args) {
		
		Box b = new Box(0,0,1,0);
		b.rotate( 5400000); // 90 degrees
		System.out.println(  );
	}
	
}
