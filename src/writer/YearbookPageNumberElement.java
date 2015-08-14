package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Model for page numbers. A yearbook should have one of these, which has layout
 * and font information.
 * @author Cody Crow
 *
 */
public class YearbookPageNumberElement extends YearbookTextElement implements Serializable {
	private static final long serialVersionUID = 4422039882251067L;

	PageNumberLocations location;

	public YearbookPageNumberElement(int pageWidth, int pageHeight) {
		super(pageWidth, pageHeight);
		location = PageNumberLocations.DOWN_MIDDLE;
	}

	public static Rectangle generateBounds(int pageWidth, int pageHeight, PageNumberLocations location, int activePage, Point point, Point offset) {
		int pageNumberX, pageNumberY;
		switch (location) {
		case UP_IN:
		case UP_MIDDLE:
		case UP_OUT:
			pageNumberY = (int) ((pageHeight + point.y) * (1.0 - .982712));
			break;
		case DOWN_IN:
		case DOWN_MIDDLE:
		case DOWN_OUT:
			pageNumberY = (int) ((pageHeight - point.y) * .982712);
			break;
		default:
			pageNumberY = 0;
			break;
		}

		switch (location) {
		case DOWN_IN:
		case UP_IN:
			//Left pages
			if (activePage % 2 == 1) {
				pageNumberX = (int) ((pageWidth - point.x) * .95);
			} else {
				pageNumberX = (int) ((pageWidth - point.x) * .05);
			}
			break;
		case DOWN_MIDDLE:
		case UP_MIDDLE:
			pageNumberX = (int) ((pageWidth - point.x) / 2.0);
			break;
		case DOWN_OUT:
		case UP_OUT:
			//Right pages
			if (activePage % 2 != 1) {
				pageNumberX = (int) ((pageWidth - point.x) * .95);
			} else {
				pageNumberX = (int) ((pageWidth - point.x) * .05);
			}
			break;
		default:
			pageNumberX = 0;
			break;
		}
		
		Rectangle rekt = new Rectangle(pageNumberX + offset.x, pageNumberY + offset.y, 30, 30);
		
		return rekt;
	}


}
