
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;


public class YearbookImageElement extends YearbookElement {
	//Percentage values
	public double scale;
	
	public Image getImage() {
		return image;
	}

	private int clientWidth;
	
	private Display display;
	
	private int pageWidth;
	private int pageHeight;
	private Image image;
	public String fileName;
	
	public YearbookImageElement(Display display, String fileName, int pageWidth, int pageHeight) {
		scale = 1;
		image = new Image(display, fileName);
		clientWidth = display.getClientArea().width;
		this.display = display;
		
		this.x = 0;
		this.y = 0;
		this.rotation = 0;
		
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.fileName = fileName;
	}
	
	@Override
	boolean isImage() {
		return true;
	}

	@Override
	boolean isAtPoint(int x, int y) {
		return this.getBounds().contains(x, y);
	}
	
	/**
	 * The bounds of the image as it is displayed *on the canvas*.
	 * @return Rectangle image bounds
	 */
	public Rectangle getBounds() {
		int xc = (int) (this.x * this.pageWidth);
		int yc = (int) (this.y * this.pageHeight);
		int width = (int) (this.image.getBounds().width * this.scale);
		int height = (int) (this.image.getBounds().height * this.scale);
		Rectangle bounds = new Rectangle(xc, yc, width, height);
		return bounds;
	}
}
