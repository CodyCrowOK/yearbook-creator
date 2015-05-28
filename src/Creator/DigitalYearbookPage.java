package Creator;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * Holds the necessary information for a yearbook page.
 * @author Cody Crow
 *
 */
public class DigitalYearbookPage implements Serializable {
	private static final long serialVersionUID = 1357891742345222748L;
	
	transient Image image;
	ArrayList<YearbookClickableElement> clickables;
	
	public DigitalYearbookPage(Image image,	ArrayList<YearbookClickableElement> clickables) {
		this.image = image;
		this.clickables = clickables;
	}
	
	/*
	private void writeObject(ObjectOutputStream out) throws IOException {
	        out.defaultWriteObject();
	        out.writeInt(images.size()); // how many images are serialized?
	        for (BufferedImage eachImage : images) {
	            ImageIO.write(eachImage, "png", out); // png is lossless
	        }
	    }
	
	    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	        in.defaultReadObject();
	        final int imageCount = in.readInt();
	        images = new ArrayList<BufferedImage>(imageCount);
	        for (int i=0; i<imageCount; i++) {
	            images.add(ImageIO.read(in));
	        }
	    }
	*/
	
	/**
	 * Converts the image to an AWT image before saving.
	 * @param out ObjectOutputStream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		BufferedImage awtImage = SWTUtils.convertToAWT(image.getImageData());
		ImageIO.write(awtImage, "png", out);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		ImageData data = SWTUtils.convertToSWT(ImageIO.read(in));
		Display display = new Display();
		this.image = new Image(display, data);
	}
}
