import java.util.ArrayList;


public class Yearbook {
	String name;
	ArrayList<YearbookPage> pages;
	YearbookSettings settings;
	
	public Yearbook(String name) {
		this.name = name;
		pages = new ArrayList<YearbookPage>();
		pages.add(new YearbookPage(name));
		settings = new YearbookSettings();
	}
}
