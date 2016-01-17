package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Defines an image box.
 * An image box is a rectangle that is drawn on the page within its bounds.
 * It can contain a YearbookImageElement which is scaled and positioned relative
 * to the bounds of the ImageBoxElement.
 * @author Cody Crow
 *
 */
public class ImageBoxElement extends YearbookElement implements Serializable {
	private static final long serialVersionUID = -2345346947340949879L;
	public YearbookImageElement imageElement;
	
	//private double x;
	//private double y;
	public double width;
	public double height;
	public RGB rgb;
	public int alpha; //0 to 255
	public boolean pan;
	
	transient private Color bgColor;
	
	public ImageBoxElement(double x, double y, double w, double h) {
		construct(x, y, w, h);
	}
	
	public ImageBoxElement(double x, double y, double w, double h, int pageWidth, int pageHeight) {
		this(x, y, w, h);
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
		
	}
	
	/**
	 * 
	 * @param bounds Element bounds in pixels
	 * @param pageWidth
	 * @param pageHeight
	 */
	public ImageBoxElement(Rectangle bounds, int pageWidth, int pageHeight) {
		double x, y, w, h;
		x = (double) bounds.x / pageWidth;
		y = (double) bounds.y / pageHeight;
		w = (double) bounds.width / pageWidth;
		h = (double) bounds.height / pageHeight;
		construct(x, y, w, h);
	}
	
	private void construct(double x, double y, double w, double h) {
		this.border = new YearbookElementBorder();
		alpha = 255;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		pan = false;
	}
	
	public boolean hasImage() {
		return imageElement != null;
	}
	
	public boolean hasRGB() {
		return rgb != null;
	}
	
	private boolean hasColor() {
		return bgColor != null;
	}
	
	public Color getColor(Device device) {
		if (this.hasRGB()) {
			if (!this.hasColor()) this.bgColor = new Color(device, rgb);
		} else {
			this.bgColor = new Color(device, 0xff, 0xff, 0xff);
		}
		return this.bgColor;
	}
	
	public void setRGB(RGB rgb) {
		this.rgb = rgb;
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
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		int xc = (int) (this.x * pageWidth);
		int yc = (int) (this.y * pageHeight);
		int width = (int) (this.width * pageWidth);
		int height = (int) (this.height * pageHeight);
		Rectangle bounds = new Rectangle(xc, yc, width, height);
		return bounds;
	}

	@Override
	boolean isAtPoint(int x, int y) {
		return this.getBounds().contains(x, y);
	}

	@Override
	boolean isAtPoint(int x, int y, int pageWidth, int pageHeight) {
		return this.getBounds(pageWidth, pageHeight).contains(x, y);
	}

	@Override
	public void setLocationRelative(int x, int y) {
		this.x = (double) (x + this.x) / this.pageWidth;
		this.y = (double) (y + this.y) / this.pageHeight;
	}

	@Override
	public void resize(Display display, int x, int y) {
		this.width = (double) (x + this.getBounds().width) / this.pageWidth;
		this.height = (double) (y + this.getBounds().height) / this.pageHeight; 
	}

	@Override
	public YearbookElement copy() {
		ImageBoxElement e = new ImageBoxElement(this.x, this.y, this.width, this.height);
		e.border = this.border;
		if (this.hasImage()) e.imageElement = (YearbookImageElement) this.imageElement.copy();
		e.alpha = this.alpha;
		e.rgb = this.rgb;
		e.rotation = this.rotation;
		e.shadow = this.shadow;
		e.pageHeight = this.pageHeight;
		e.pageWidth = this.pageWidth;
		return e;
	}
	
	/*
	public void drawImage(GC gc, int pageWidth, int pageHeight) {
		if (!this.hasImage()) return;
		
		Image image = this.imageElement.getImage();
		int destX, destY, srcX, srcY, srcWidth, srcHeight, destWidth, destHeight;
		/*
		 * Initialize to where variables would be if image was the same size as box.
		 *
		
		srcX = srcY = 0;
		destX = this.getBounds(pageWidth, pageHeight).x;
		destY = this.getBounds(pageWidth, pageHeight).y;
		srcWidth = this.imageElement.getBounds(pageWidth, pageHeight).width;
		srcHeight = this.imageElement.getBounds(pageWidth, pageHeight).height;
		destWidth = this.getBounds(pageWidth, pageHeight).width;
		destHeight = this.getBounds(pageWidth, pageHeight).height;
		
		/*
		 * Adjust for other factors.
		 *
		
		if (this.imageElement.getBounds(pageWidth, pageHeight).x > 0) {
			destX += this.imageElement.getBounds(pageWidth, pageHeight).x;
		} else {
			srcX += Math.abs(this.imageElement.getBounds(pageWidth, pageHeight).x);
		}
		
		if (this.imageElement.getBounds(pageWidth, pageHeight).y > 0) {
			destY += this.imageElement.getBounds(pageWidth, pageHeight).y;
		} else {
			srcY += Math.abs(this.imageElement.getBounds(pageWidth, pageHeight).y);
		}
		
		int a = Math.abs(this.imageElement.getBounds(pageWidth, pageHeight).x);
		int b = this.getBounds(pageWidth, pageHeight).width;
		int d = this.imageElement.getBounds(pageWidth, pageHeight).width;
		int c = d - (a + b);
		
		if (this.imageElement.getBounds(pageWidth, pageHeight).width - a > b) {
			destWidth -= c;
			destWidth = Math.abs(destWidth);
		}
		
		a = Math.abs(this.imageElement.getBounds(pageWidth, pageHeight).y);
		b = this.getBounds(pageWidth, pageHeight).height;
		d = this.imageElement.getBounds(pageWidth, pageHeight).height;
		c = d - (a + b);
		
		if (this.imageElement.getBounds(pageWidth, pageHeight).height - a > b) {
			destHeight -= c;
			destHeight = Math.abs(destHeight);
		}
		
		
		boolean draw = true;
		try {
			assertion(srcX >= 0);
			assertion(srcY >= 0);
			assertion(srcWidth > 0);
			assertion(srcHeight > 0);
			assertion(destX >= 0);
			assertion(destY >= 0);
			assertion(destWidth > 0);
			assertion(destHeight > 0);
		} catch (Exception e) {
			Logger.printStackTrace(e);
			draw = false;
		}
		
		if (draw) gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight);
		//TODO: Make this work on edges
		
	}*/
	
	@Override
	public void dispose() {
		
		if (imageElement != null) imageElement.dispose();
		
	}
	
	private void assertion(boolean e) throws Exception {
		if (!e) {
			throw new Exception("Assertion failed.");
		}
	}

	public void setZoom(double z) {
		//Screw it.
		this.imageElement.scale = z;
	}

}
