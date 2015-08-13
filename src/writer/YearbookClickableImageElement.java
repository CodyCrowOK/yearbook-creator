package writer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/**
 * Represents an image which can be clicked to open a video.
 * @author Cody Crow
 *
 */
public class YearbookClickableImageElement extends YearbookImageElement implements Clickable, Serializable {

	private static final long serialVersionUID = -2109379287205311724L;
	public Deque<Video> videos;
	
	public YearbookClickableImageElement copy() {
		YearbookClickableImageElement copy = new YearbookClickableImageElement(this.getDisplay(), this.imageData, this.pageWidth, this.pageHeight);
		copy.videos = videos;
		copy.x = this.x;
		copy.y = this.y;
		copy.rotation = this.rotation;
		copy.scale = this.scale;
		return copy;
	}
	
	public YearbookClickableImageElement(Display display, String fileName,
			int pageWidth, int pageHeight) {
		super(display, fileName, pageWidth, pageHeight);
		videos = new ArrayDeque<Video>();
	}

	public YearbookClickableImageElement(Display display,
			ImageData imageData, int pageWidth, int pageHeight) {
		super(display, imageData, pageWidth, pageHeight);
		videos = new ArrayDeque<Video>();
	}

	@Override
	public Deque<Video> getVideos() {
		return videos;
	}
	
	@Override
	public boolean isClickable() {
		return true;
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
