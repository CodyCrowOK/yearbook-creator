package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import pspa.BoxModel;

/**
 * Wrapper for a BoxModel which can be inserted into a yearbook page.
 * @author Cody Crow
 *
 */
public class BoxModelElement extends YearbookElement implements Serializable {
	private static final long serialVersionUID = -5785943315143299833L;
	public BoxModel boxModel;
	
	public BoxModelElement(BoxModel bm) {
		boxModel = bm;
	}
	
	@Override
	public Rectangle getBounds() {
		return new Rectangle(0, 0, 0, 0);
	}

	@Override
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		return new Rectangle(0, 0, 0, 0);
	}

	@Override
	boolean isAtPoint(int x, int y) {
		return false;
	}

	@Override
	boolean isAtPoint(int x, int y, int pageWidth, int pageHeight) {
		return false;
	}

	@Override
	public void setLocationRelative(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(Display display, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public YearbookElement copy() {
		return this;
	}

}
