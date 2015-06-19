package writer;

import java.io.Serializable;

/**
 * Represents the settings for a single yearbook.
 * @author Cody Crow
 *
 */
public class YearbookSettings implements Serializable {
	private static final long serialVersionUID = 3913393088336859492L;
	public int width;
	public int height;
	public boolean showPageNumbers;
	
	/**
	 * Constructor
	 * @param width The width of the working canvas in pixels.
	 * @param height The height of the working canvas in pixels.
	 */
	public YearbookSettings(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	public YearbookSettings() {}
	
	/**
	 * The width in pixels of the published version.
	 * @return the width in pixels
	 */
	public int publishWidth() {
		return (int) (8.5 * 300);
	}
	
	/**
	 * The height in pixels of the published version.
	 * @return the height in pixels
	 */
	public int publishHeight() {
		return 11 * 300;
	}
	
	public double xInches() {
		return 8.5;
	}
	
	public double yInches() {
		return 11.0;
	}
	
}
