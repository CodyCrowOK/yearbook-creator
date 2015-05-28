package writer;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

public class YearbookPage {
	private ArrayList<YearbookElement> elements;
	public String name;
	public Image backgroundImage;
	
	public YearbookPage(Image backgroundImage) {
		this();
		this.name = "";
		this.backgroundImage = backgroundImage;
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
	}
	
	public void addElement(YearbookElement e) {
		elements.add(e);
	}
	
	public String toString() {
		return this.name;
	}
	
	public boolean isElementAtPoint(int x, int y) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y)) return true;
		}
		return false;
	}
	
	public YearbookElement getElementAtPoint(int x, int y) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			YearbookElement e = elements.get(i);
			if (e.isAtPoint(x, y)) return e;
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
}
