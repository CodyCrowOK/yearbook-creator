package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;

public class YearbookElementPrototype implements Serializable {
	private static final long serialVersionUID = -558113021186529450L;
	public double x;
	public double y;
	public float rotation;
	
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		int xc = (int) (this.x * pageWidth);
		int yc = (int) (this.y * pageHeight);
		int width, height;
		width = height = 30;
		return new Rectangle(xc, yc, width, height);
	}
	
	public boolean isAtPoint(int x, int y, int pageWidth, int pageHeight) {
		return this.getBounds(pageWidth, pageHeight).contains(x, y);
	}
}
