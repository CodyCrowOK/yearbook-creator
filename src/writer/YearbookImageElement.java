package writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/**
 * Represents an image inserted into the canvas.
 * @author Cody Crow
 *
 */
public class YearbookImageElement extends YearbookElement implements Serializable {
	private static final long serialVersionUID = 8808543926557894799L;

	//Percentage values
	public double scale;
	
	public Image getImage(Display display) {
		if (image == null) {
			image = new Image(display, imageData);
		}
		return image;
	}

	private int clientWidth;
	
	transient private Display display;
	
	private int pageWidth;
	private int pageHeight;
	transient ImageData imageData;
	transient private Image image;
	public String fileName;
	
	public YearbookImageElement(Display display, String fileName, int pageWidth, int pageHeight) {
		generateRandomElementId();
		scale = 1;
		Image tmp = new Image(display, fileName);
		imageData = tmp.getImageData();
		tmp.dispose();
		image = new Image(display, imageData);
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
		//System.out.println("x y " + bounds.x + "," + bounds.y);
		return bounds;
	}

	/**
	 * The bounds of the image as it is displayed on a canvas of given
	 * dimensions.
	 * @param pageWidth The given page width
	 * @param pageHeight The given page height
	 * @return The new image bounds
	 */
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		int xc = (int) (this.x * pageWidth);
		int yc = (int) (this.y * pageHeight);
		int width = (int) ((this.image.getBounds().width * this.scale) * ((double) pageWidth / this.pageWidth));
		int height = (int) ((this.image.getBounds().height * this.scale) * ((double) pageHeight / this.pageHeight));
		Rectangle bounds = new Rectangle(xc, yc, width, height);
		//System.out.println("x y " + bounds.x + "," + bounds.y);
		return bounds;
	}
	
	/**
	 * Sets the image scale relative to the current scale.
	 * @param scale the amount to multiply the current scale by
	 */
	public void setScaleRelative(double scale) {
		this.scale *= scale;
	}
	
	public void setLocationRelative(int x, int y) {
		this.x = (double) x / this.pageWidth;
		this.y = (double) y / this.pageHeight;
	}
	
	/*
	 * Serialization methods
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (this.imageData == null) {
			this.imageData = YearbookImages.bogusBackgroundData();
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { this.imageData };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageLoader.save(stream, SWT.IMAGE_PNG);
		byte[] bytes = stream.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		ImageLoader imageLoader = new ImageLoader();
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		ImageData[] data = imageLoader.load(stream);
		this.imageData = data[0];
	}
}
