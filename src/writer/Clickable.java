package writer;
import java.util.Deque;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementing Clickable allows users to click on the graphical representation
 * of the given object and be taken to a video. 
 * @author Cody Crow
 *
 */
public interface Clickable {
	/**
	 * 
	 * @return The clickable area of the object.
	 */
	Rectangle getBounds();
	Deque<Video> getVideos();
}
