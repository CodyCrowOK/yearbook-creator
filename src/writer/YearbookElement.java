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
	
	public int zIndex; //Describes how the element sits relative to other elements.
	
	//Percentage values
	protected double x;
	protected double y;
	
	public double rotation;
	
	
	
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
	 * @param x
	 * @param y
	 * @return true if the point (x, y) is within the bounds of the element.
	 */
	abstract boolean isAtPoint(int x, int y);
	
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
	
	protected void generateRandomElementId() {
		elementId = (long) (Math.random() * (Math.pow(2, 63) - 1));
	}
	
	/**
	 * 
	 * @return false unless overridden by a clickable subclass
	 */
	public boolean isClickable() {
		return false;
	}
}
