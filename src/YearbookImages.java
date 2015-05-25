import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class YearbookImages {

	static Image newDocument(Display display) {
		return new Image(display, "icons/large/document-new.png");
	}
	
	static Image openDocument(Display display) {
		return new Image(display, "icons/large/document-open.png");
	}
}
