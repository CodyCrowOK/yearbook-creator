package writer;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is serialized and loaded by the yearbook viewer.
 * @author Cody Crow
 *
 */
public class DigitalYearbook implements Serializable {
	private static final long serialVersionUID = 6436143573225461026L;

	public ArrayList<DigitalYearbookPage> pages;
	public String name;
	
	public DigitalYearbook(String name) {
		this.name = name;
		this.pages = new ArrayList<DigitalYearbookPage>();
	}
	
	
}
