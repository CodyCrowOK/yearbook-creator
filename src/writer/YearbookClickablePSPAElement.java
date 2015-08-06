package writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import pspa.Person;
import pspa.Volume;

public class YearbookClickablePSPAElement extends YearbookClickableImageElement implements Clickable, Serializable {
	private static final long serialVersionUID = 9035596712953989375L;

	
	public YearbookTextElement text;
	public Person person;
	public Point margins;

	public YearbookClickablePSPAElement(Display display,
			ImageData imageData, int pageWidth, int pageHeight) {
		super(display, imageData, pageWidth, pageHeight);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Serialization methods
	 */
	
	public YearbookClickablePSPAElement(Display display,
			ImageData imageData, int pageWidth, int pageHeight,
			YearbookPSPAElement element) {
		this(display, imageData, pageWidth, pageHeight);
		this.text = element.text;
		this.person = element.person;
		this.margins = element.margins;
	}

	private void computeBounds(Volume volume, int pageWidth, int pageHeight) {
		margins = Volume.photoSpacing(volume.grid, pageWidth, pageHeight);
		int width = margins.x * (volume.grid.x - 1);
		this.setScaleRelative((double) 1.4 * width / pageWidth);
		
	}

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
