package writer;
import org.eclipse.swt.widgets.Display;


public class YearbookClickableImageElement extends YearbookImageElement implements Clickable {

	private Video video;
	
	public YearbookClickableImageElement(Display display, String fileName,
			int pageWidth, int pageHeight) {
		super(display, fileName, pageWidth, pageHeight);
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
