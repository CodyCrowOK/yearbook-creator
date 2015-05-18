import java.util.ArrayList;


public class Yearbook {
	String name;
	ArrayList<YearbookPage> pages;
	YearbookSettings settings;
	int activePage;
	
	public Yearbook(String name) {
		this.name = name;
		pages = new ArrayList<YearbookPage>();
		pages.add(new YearbookPage(name));
		settings = new YearbookSettings();
		activePage = 0;
	}
}
