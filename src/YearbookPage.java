import java.util.ArrayList;

public class YearbookPage {
	private ArrayList<YearbookElement> elements;
	
	public String name;
	
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
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y)) return e;
		}
		return null;
	}
}
