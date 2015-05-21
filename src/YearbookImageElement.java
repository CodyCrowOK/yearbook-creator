import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;


public class YearbookImageElement extends YearbookElement {
	//Percentage values
	public double scale;
	
	public Image getImage() {
		return image;
	}

	private int clientWidth;
	
	private Display display;
	
	public YearbookImageElement(Display display, String fileName) {
		scale = 1.0;
		image = new Image(display, fileName);
		clientWidth = display.getClientArea().width;
		this.display = display;
		
		super.x = 0;
		super.y = 0;
		super.rotation = 0;
	}

	@Override
	IFigure figure() {
		return new ImageFigure(image);
	}
	
	@Override
	boolean isImage() {
		return true;
	}
}
