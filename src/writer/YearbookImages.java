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
	
	/**
	 * This is bogus ImageData that is written to disk whenever ImageData
	 * would otherwise be null. This is a necessary implication of the way
	 * Java does serialization. It is not rendered to the screen, and is
	 * removed upon opening the file.
	 * @return ImageData
	 */
	static ImageData bogusBackgroundData() {
		PaletteData paletteData = new PaletteData(new RGB[] {new RGB(151,245,76), new RGB(9,70,121)});
		ImageData tmp = new ImageData(1,1,1,paletteData);
		tmp.alpha = 17;
		return tmp;
	}
}
