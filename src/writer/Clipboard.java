package writer;

import java.util.ArrayList;

public class Clipboard {
	
	boolean cut;
	
	/**
	 * Selected elements
	 */
	public ArrayList<YearbookElement> elements;
	
	/**
	 * Elements that have been cut/copied from the page
	 */
	public ArrayList<YearbookElement> cutElements;
	
	public Clipboard() {
		elements = new ArrayList<YearbookElement>();
		cutElements = new ArrayList<YearbookElement>();
	}
	
}
