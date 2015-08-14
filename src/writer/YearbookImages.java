package writer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Contains static methods which return Images used in the GUI.
 * @author Cody Crow
 *
 */
public class YearbookImages {

	static Image newDocument(Display display) {
		return new Image(display, "icons/large/document-new.png");
	}
	
	static Image openDocument(Display display) {
		return new Image(display, "icons/large/document-open.png");
	}
	
	static Image importPDF(Display display) {
		return new Image(display, "icons/large/pdf.png");
	}
	
	public static Image logo(Display display) {
		return new Image(display, "icons/large/logo.png");
	}
	
	public static Image readerBackground(Display display) {
		return new Image(display, "icons/large/reader-background-new.png");
	}
	
	public static Image openBook(Display display) {
		return new Image(display, "icons/large/open-book-small.png");
	}
	
	public static Image logoWhiteBackground(Display display) {
		return new Image(display, "icons/large/logo-whitebg.png");
	}
	
	public static Image creatorBackground(Display display) {
		return new Image(display, "icons/large/creator-background.png");
	}
	
	/**
	 * This is bogus ImageData that is written to disk whenever ImageData
	 * would otherwise be null. This is a necessary implication of the way
	 * Java does serialization. It is not rendered to the screen, and is
	 * removed upon opening the file.
	 * @return ImageData
	 */
	static ImageData bogusBackgroundData() {
		PaletteData paletteData = new PaletteData(new RGB[] {new RGB(0xff, 0xff, 0xff), new RGB(0xff, 0xff, 0xff)});
		ImageData tmp = new ImageData(1,1,1,paletteData);
		tmp.alpha = 0xff;
		return tmp;
	}
}
