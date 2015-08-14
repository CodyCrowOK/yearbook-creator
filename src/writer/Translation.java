package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.Point;

public class Translation implements Serializable {
	private static final long serialVersionUID = -2915119203637042921L;
	public double x;
	public double y;
	public boolean visible;
	
	public Translation() {
		visible = true;
		x = y = 0;
	}
	
	public Point getDiff(int pageWidth, int pageHeight) {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		return new Point(x, y);
	}
	
	public void set(int xDiff, int yDiff, int pageWidth, int pageHeight) {
		x = (double) xDiff / pageWidth;
		y = (double) yDiff / pageHeight;
	}
}
