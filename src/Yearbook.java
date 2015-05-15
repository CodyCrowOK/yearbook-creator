import java.util.ArrayList;


public class Yearbook {
	String name;
	ArrayList<YearbookPage> pages;
	
	public Yearbook(String name) {
		this.name = name;
		pages = new ArrayList<YearbookPage>();
		pages.add(new YearbookPage(name));
	}
}
