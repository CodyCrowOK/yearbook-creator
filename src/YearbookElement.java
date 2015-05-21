import org.eclipse.draw2d.IFigure;
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
	
	protected Image image;
	
	/**
	 * 
	 * @return the representation of the element on the canvas.
	 */
	abstract IFigure figure();
	
	boolean isImage() {
		return false;
	}
}
