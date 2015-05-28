package writer;
import java.io.Serializable;

import org.eclipse.swt.graphics.*;

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
	
	
	
	boolean isImage() {
		return false;
	}

	abstract public Rectangle getBounds();
	abstract boolean isAtPoint(int x, int y);
	
	/**
	 * Sets the location relative to the current page (i.e. in pixels)
	 * @param x the x-coordinate on the canvas
	 * @param y the y-coordinate on the canvas
	 */
	abstract public void setLocationRelative(int x, int y);
	
	protected void generateRandomElementId() {
		elementId = (long) (Math.random() * (Math.pow(2, 63) - 1));
	}
	
	public boolean isClickable() {
		return false;
	}
}
