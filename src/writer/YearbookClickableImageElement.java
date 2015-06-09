package writer;
import java.io.Serializable;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * Represents an image which can be clicked to open a video.
 * @author Cody Crow
 *
 */
public class YearbookClickableImageElement extends YearbookImageElement implements Clickable, Serializable {

	private static final long serialVersionUID = -2109379287205311724L;
	public Video video;
	
	public YearbookClickableImageElement(Display display, String fileName,
			int pageWidth, int pageHeight) {
		super(display, fileName, pageWidth, pageHeight);
	}

	public YearbookClickableImageElement(Display display,
			ImageData imageData, int pageWidth, int pageHeight) {
		super(display, imageData, pageWidth, pageHeight);
	}

	@Override
	public Video getVideo() {
		return video;
	}
	
	@Override
	public boolean isClickable() {
		return true;
	}

	
}
