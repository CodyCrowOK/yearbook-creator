import java.util.ArrayList;


public class YearbookPage {
	private ArrayList<YearbookElement> elements;
	public String name;

	public YearbookPage() {
		elements = new ArrayList<YearbookElement>();
	}

	public YearbookPage(String name) {
		this();
		this.name = name;
	}
}
