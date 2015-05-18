import java.util.ArrayList;

import org.eclipse.draw2d.LayeredPane;


public class YearbookPage {
	private ArrayList<YearbookElement> elements;
	public String name;
	private LayeredPane layers;

	public YearbookPage() {
		elements = new ArrayList<YearbookElement>();
		layers = new LayeredPane();
	}

	public YearbookPage(String name) {
		this();
		this.name = name;
	}
}
