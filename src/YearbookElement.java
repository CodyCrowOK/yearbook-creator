import org.eclipse.swt.graphics.Image;

/**
 * Abstract class that represents a single item on a yearbook page.
 * Items could be images, text, or whatever else.
 * @author Cody Crow
 *
 */
abstract public class YearbookElement {
	public int zIndex; //Describes how the element sits relative to other elements.
	
	//Percentage values
	public double x;
	public double y;
	
	public double rotation;
	
	
	
	boolean isImage() {
		return false;
	}

	abstract boolean isAtPoint(int x, int y);
}
