package writer;
import java.io.Serializable;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract class that represents a single item on a yearbook page.
 * Items could be images, text, or whatever else.
 * @author Cody Crow
 *
 */
abstract public class YearbookElement implements Serializable {
	private static final long serialVersionUID = -8472886681905623636L;

	/**
	 * A unique element id which is used to make comparisons correct. This
	 * prevents two elements with the same image from being perceived as
	 * the same element.
	 */
	long elementId;
	
	//Percentage values
	protected double x;
	protected double y;
	
	public float rotation;
	
	public int pageWidth;
	public int pageHeight;
	
	public YearbookElementBorder border;
	public boolean shadow;
	
	
	
	public boolean isImage() {
		return false;
	}

	/**
	 * 
	 * @return the bounds of the given element
	 */
	abstract public Rectangle getBounds();
	
	/**
	 * 
	 * @param pageWidth
	 * @param pageHeight
	 * @return the bounds of the element dependent on the page dimensions
	 */
	abstract public Rectangle getBounds(int pageWidth, int pageHeight);
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return true if the point (x, y) is within the bounds of the element.
	 */
	abstract boolean isAtPoint(int x, int y);
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param pageWidth
	 * @param pageHeight
	 * @return true if the point (x, y) is within the bounds of the element
	 * on a page with the given dimensions.
	 */
	abstract boolean isAtPoint(int x, int y, int pageWidth, int pageHeight);
	
	/**
	 * Sets the location relative to the current page (i.e. in pixels)
	 * @param x the x-coordinate on the canvas
	 * @param y the y-coordinate on the canvas
	 */
	abstract public void setLocationRelative(int x, int y);
	
	/**
	 * Resizes an element using relative x and y values
	 * @param x the x-value on the canvas
	 * @param y the y-value on the canvas
	 */
	abstract public void resize(Display display, int x, int y);
	
	/**
	 * Used for copying yearbook elements
	 * @return the new copy of the element
	 */
	abstract public YearbookElement copy();
	
	/**
	 * Dispose of system resources
	 */
	abstract public void dispose();
	
	public void generateRandomElementId() {
		elementId = (long) (Math.random() * (Math.pow(2, 63) - 1));
	}
	
	/**
	 * 
	 * @return false unless overridden by a clickable subclass
	 */
	public boolean isClickable() {
		return false;
	}
	
	/**
	 * 
	 * @return false unless overridden by a text-based subclass
	 */
	public boolean isText() {
		return false;
	}
	
	public boolean isPSPA() {
		return false;
	}
	
	public boolean is(YearbookElement e) {
		return e.elementId == elementId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		try {
			YearbookElement e = (YearbookElement) o;
			if (e.x != this.x) {
				return false;
			}
			if (e.y != this.y) {
				return false;
			}
			if (e.rotation != this.rotation) {
				return false;
			}
			if (e.isClickable() != this.isClickable()) {
				return false;
			}
			if (e.isImage() != this.isImage()) {
				return false;
			}
			if (e.isText() != this.isText()) {
				return false;
			}
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * *Sigh*
	 * @return false unless overridden
	 */
	public boolean isTruePSPA() {
		return false;
	}
}
