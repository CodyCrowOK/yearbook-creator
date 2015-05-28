package writer;

import java.io.Serializable;

public class YearbookSettings implements Serializable {
	private static final long serialVersionUID = 3913393088336859492L;
	public int width;
	public int height;
	
	public YearbookSettings(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	public YearbookSettings() {}
	
	public int publishWidth() {
		return (int) (8.5 * 300);
	}
	
	public int publishHeight() {
		return 11 * 300;
	}
	
}
