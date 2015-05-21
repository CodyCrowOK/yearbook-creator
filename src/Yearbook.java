import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;


public class Yearbook {
	private ArrayList<YearbookPage> pages;
	private Image defaultBackground;
	
	String name;
	YearbookSettings settings;
	int activePage;
	
	public Yearbook(String name) {
		this.name = name;
		pages = new ArrayList<YearbookPage>();
		pages.add(new YearbookPage(name));
		settings = new YearbookSettings();
		activePage = 0;
		defaultBackground = null;
	}
	
	public YearbookPage page(int index) {
		return pages.get(index);
	}
	
	public void addPage(String name) {
		pages.add(new YearbookPage(name));
	}
	
	public void removePage(int index) {
		pages.remove(index);
	}
	
	public int size() {
		return pages.size();
	}
	
	public void movePage(int source, int destination) {
		if (destination > size()) return;
		YearbookPage page = pages.get(source);
		pages.remove(source);
		pages.add(destination, page);
	}
}
