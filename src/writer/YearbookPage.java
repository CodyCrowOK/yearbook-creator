package writer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Represents a single page in a yearbook.
 * @author Cody Crow
 *
 */
public class YearbookPage implements Serializable {
	private static final long serialVersionUID = -5090460491486388571L;
	private ArrayList<YearbookElement> elements;
	public String name;
	transient private ImageData backgroundImageData;
	transient private Image backgroundImage;
	public boolean noBackground;

	public YearbookPage(Image backgroundImage) {
		this();
		this.name = "";
		this.backgroundImageData = backgroundImage.getImageData();
	}

	public YearbookPage(ImageData backgroundImage) {
		this();
		this.name = "";
		this.backgroundImageData = backgroundImage;
	}

	public YearbookElement element(int index) {
		return elements.get(index);
	}

	public ArrayList<YearbookElement> getElements() {
		return elements;
	}

	public YearbookPage() {
		elements = new ArrayList<YearbookElement>();
	}

	public YearbookPage(String name) {
		this();
		this.name = name;
		this.noBackground = true;
	}

	public void addElement(YearbookElement e) {
		elements.add(e);
	}

	public Image backgroundImage(Display display) {
		//Try not to leak too many resources...
		if (display == null || this.backgroundImageData == null) return null; 
		if (this.backgroundImage != null && !this.backgroundImage.isDisposed()) {
			if (this.backgroundImage.getImageData() != this.backgroundImageData) {
			}
		} else {
			this.backgroundImage = new Image(display, this.backgroundImageData);
		}
		return this.backgroundImage;
	}

	public String toString() {
		return this.name;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return true if exists an element on the working canvas at (x, y)
	 */
	public boolean isElementAtPoint(int x, int y) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y)) return true;
		}
		return false;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param pageWidth
	 * @param pageHeight
	 * @return true if element exists at (x, y) on canvas of given dimensions
	 */
	public boolean isElementAtPoint(int x, int y, int pageWidth, int pageHeight) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y, pageWidth, pageHeight)) return true;
		}
		return false;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return true if there is a clickable region containing (x, y)
	 */
	public boolean isClickableAtPoint(int x, int y) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y) && e.isClickable()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param pageWidth
	 * @param pageHeight
	 * @return true if there is a clickable region containing (x, y) in the given page size
	 */
	public boolean isClickableAtPoint(int x, int y, int pageWidth, int pageHeight) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y, pageWidth, pageHeight) && e.isClickable()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds the element at a given point on the working canvas (x, y)
	 * @param x
	 * @param y
	 * @return the found element, or null if no element found
	 */
	public YearbookElement getElementAtPoint(int x, int y) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			YearbookElement e = elements.get(i);
			if (e.isAtPoint(x, y)) return e;
		}
		return null;
	}

	public YearbookElement getElementAtPoint(int x, int y, int pageWidth, int pageHeight) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			YearbookElement e = elements.get(i);
			if (e.isAtPoint(x, y, pageWidth, pageHeight)) return e;
		}
		return null;
	}

	/**
	 * Finds the element on the page which is equal to e
	 * @param e the YearbookElement to compare to
	 * @return the found YearbookElement on the page
	 */
	public YearbookElement findElement(YearbookElement e) {
		for (int i = 0; i < this.elements.size(); i++) {
			if (this.elements.get(i) == e) {
				return this.elements.get(i);
			}
		}
		return null;
	}

	/**
	 * Finds the element on the page which is equal to e
	 * @param e the YearbookElement to compare to
	 * @return the found YearbookElement on the page
	 */
	public int findElementIndex(YearbookElement e) {
		for (int i = 0; i < this.elements.size(); i++) {
			if (this.elements.get(i) == e) {
				return i;
			}
		}
		return -1;
	}

	public ArrayList<YearbookElement> getElementsInRectangle(Rectangle r, int pageWidth, int pageHeight) {
		ArrayList<YearbookElement> ret = new ArrayList<YearbookElement>();
		for (YearbookElement e : elements) {
			Rectangle bounds = e.getBounds(pageWidth, pageHeight);

			if (rectCheckOverlap(bounds, r)) {
				ret.add(e);
			}
		}
		return ret;
	}

	private boolean rectCheckOverlap(Rectangle r1, Rectangle r2) { 
		return !(r1.x + r1.width < r2.x || r1.y + r1.height < r2.y || r1.x > r2.x + r2.width || r1.y > r2.y + r2.height);
	}

	/*
	 * Serialization methods
	 */

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (this.backgroundImageData == null) {
			this.noBackground = true;
			this.backgroundImageData = YearbookImages.bogusBackgroundData();
		} else {
			this.noBackground = false;
		}
		out.defaultWriteObject();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { this.backgroundImageData };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageLoader.save(stream, SWT.IMAGE_PNG);
		byte[] bytes = stream.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private void readObject(ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		ImageLoader imageLoader = new ImageLoader();
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		ImageData[] data = imageLoader.load(stream);
		this.backgroundImageData = data[0];
		if (this.noBackground == true) {
			this.backgroundImageData = null;
		}
	}

	public void removeElement(YearbookElement selectedElement) {
		if (this.findElementIndex(selectedElement) >= 0) this.elements.remove(this.findElementIndex(selectedElement));
	}

	public void setBackgroundImageData(ImageData imageData) {
		if (this.backgroundImage != null) {
			if (!this.backgroundImage.isDisposed()) backgroundImage.dispose();
		}


		this.noBackground = false;
		this.backgroundImageData = imageData;
	}

	public void clearBackgroundImage() {
		if (this.backgroundImage != null) {
			if (!this.backgroundImage.isDisposed()) backgroundImage.dispose();
		}
		this.noBackground = true;
		this.backgroundImageData = null;
	}

	public ImageData getBackgroundImageData() {
		return backgroundImageData;
	}

	public void setInactive() {
		if (this.backgroundImage != null) {
			if (!backgroundImage.isDisposed()) backgroundImage.dispose();
		}
	}
}
