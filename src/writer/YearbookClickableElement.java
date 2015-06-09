package writer;
import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * A transparent box which can be clicked.
 * @author Cody Crow
 *
 */
public class YearbookClickableElement extends YearbookElement implements Clickable, Serializable {
	private static final long serialVersionUID = -2564949080465016227L;
	//Percentages
	private double x;
	private double y;
	private double width;
	private double height;
	
	//Pixels
	private int pageWidth;
	private int pageHeight;
	
	private Video video;
	
	public YearbookClickableElement(Video v, Rectangle rect, int pageHeight, int pageWidth) {
		this.video = v;
		this.x = ((double) rect.x / pageWidth);
		this.y = ((double) rect.y / pageHeight);
		this.width = ((double) rect.width / pageWidth);
		this.height = ((double) rect.height / pageHeight);
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
	}
	
	
	@Override
	public Video getVideo() {
		return video;
	}

	@Override
	public Rectangle getBounds() {
		int xc = (int) (this.x * this.pageWidth);
		int yc = (int) (this.y * this.pageHeight);
		int width = (int) (this.width * this.pageWidth);
		int height = (int) (this.height * this.pageHeight);
		Rectangle bounds = new Rectangle(xc, yc, width, height);
		return bounds;
	}

	@Override
	boolean isAtPoint(int x, int y) {
		if (this.getBounds().contains(x, y)) return true;
		return false;
	}

	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}


	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}


	public void setVideo(Video video) {
		this.video = video;
	}


	@Override
	/**
	 * Currently unimplemented for this element.
	 */
	public void setLocationRelative(int x, int y) {
		System.out.println("Not implemented for YearbookClickableElement.");
		
	}
	
	@Override
	public boolean isClickable() {
		return true;
	}


	@Override
	public void resize(Display display, int x, int y) {
		// TODO Auto-generated method stub
		
	}

}
