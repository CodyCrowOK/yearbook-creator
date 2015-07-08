package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.RGB;

public class YearbookElementBorder implements Serializable {
	private static final long serialVersionUID = 2683068411425055262L;

	public boolean noBorder;
	
	public RGB rgb;
	
	/**
	 * The width of the border relative to the width of the page.
	 * The width in pixels = ceil(this.width * the width of the page).
	 */
	public double width;
	
	/**
	 * The actual width in pixels of the border
	 * @param pageWidth The width of the canvas
	 * @return the pixel value of the border width
	 */
	public int getWidthInPixels(int pageWidth) {
		return (int) Math.ceil(width * pageWidth);
	}

	public YearbookElementBorder() {
		noBorder = true;
		this.rgb = new RGB(0, 0, 0);
	}
	
	public YearbookElementBorder(RGB rgb, double width) {
		noBorder = false;
		this.width = width;
		this.rgb = rgb;
	}
	
	public YearbookElementBorder(RGB rgb, int width, int pageWidth) {
		noBorder = false;
		this.width = (int) Math.ceil((double) width / pageWidth);
		this.rgb = rgb;
	}
	
	public void setWidth(int pixels, int pageWidth) {
		this.width = (double) pixels / pageWidth;
	}
	
	
}
