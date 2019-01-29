package org.pptx4j;


public class Point {
	
	public static final int extentToPixelConversionFactor = 9525; //12700;

	Point(long x, long y) {
		this.x = x;
		this.y = y;
	}

	long x;
	public long getX() {
		return x;
	}
	public String getXAsString() {
		return Long.toString(x);
	}
	public void setX(long x) {
		this.x = x;
	}

	long y;
	public long getY() {
		return y;
	}
	public String getYAsString() {
		return Long.toString(y);
	}
	public void setY(long y) {
		this.y = y;
	}

	Point add(Point point2) {
		return new Point(
				x + point2.x, y + point2.y);
	}
	Point subtract(Point point2) {
		return new Point(
				x - point2.x, y - point2.y);
	}
	
	Point toPixels() {
		return new Point( Math.round(x/extentToPixelConversionFactor),
						  Math.round(y/extentToPixelConversionFactor) );
		
	}

	public String debug() {
		return "(" + x + ", "  + y + ")";
	}
	
}
