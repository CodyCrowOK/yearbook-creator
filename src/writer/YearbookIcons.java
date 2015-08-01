package writer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Contains static methods which return the icons used in the GUI.
 * @author Cody Crow
 *
 */
public class YearbookIcons {

	static Image newDocument(Display display) {
		return new Image(display, "icons/document-new.png");
	}

	static Image open(Display display) {
		return new Image(display, "icons/document-open.png");
	}
	
	static Image copy(Display display) {
		return new Image(display, "icons/edit-copy.png");
	}

	static Image redo(Display display) {
		return new Image(display, "icons/edit-redo.png");
	}

	static Image link(Display display) {
		return new Image(display, "icons/insert-link.png");
	}

	static Image minus(Display display) {
		return new Image(display, "icons/list-remove.png");
	}

	static Image save(Display display) {
		return new Image(display, "icons/save.png");
	}

	static Image text(Display display) {
		return new Image(display, "icons/insert-text.png");
	}

	static Image undo(Display display) {
		return new Image(display, "icons/edit-undo.png");
	}

	static Image cut(Display display) {
		return new Image(display, "icons/edit-cut.png");
	}

	static Image print(Display display) {
		return new Image(display, "icons/document-print.png");
	}

	static Image exportPDF(Display display) {
		return new Image(display, "icons/document-print-preview.png");
	}

	static Image paste(Display display) {
		return new Image(display, "icons/edit-paste.png");
	}

	static Image image(Display display) {
		return new Image(display, "icons/insert-image.png");
	}

	static Image plus(Display display) {
		return new Image(display, "icons/list-add.png");
	}

	static Image video(Display display) {
		return new Image(display, "icons/video-x-generic.png");
	}
	
	static Image zoomToFit(Display display) {
		return new Image(display, "icons/zoom-fit-best.png");
	}
	
	static Image select(Display display) {
		return new Image(display, "icons/select.png");
	}
	
	static Image move(Display display) {
		return new Image(display, "icons/move.png");
	}
	
	static Image resize(Display display) {
		return new Image(display, "icons/zoom-fit-best.png");
	}
	
	static Image erase(Display display) {
		return new Image(display, "icons/delete.png");
	}
	
	static Image rotate(Display display) {
		return new Image(display, "icons/rotate.png");
	}
	
	public static Image navFront(Display display) {
		return new Image(display, "icons/go-front.png");
	}
	
	public static Image navBack(Display display) {
		return new Image(display, "icons/go-back.png");
	}
	
	public static Image navNext(Display display) {
		return new Image(display, "icons/go-next.png");
	}
	
	public static Image navEnd(Display display) {
		return new Image(display, "icons/go-end.png");
	}
	
	public static Image play(Display display) {
		return new Image(display, "icons/play.png");
	}

	public static Image pause(Display display) {
		return new Image(display, "icons/pause.png");
	}
}
