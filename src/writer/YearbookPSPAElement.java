package writer;

import java.io.Serializable;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import pspa.Person;
import pspa.Volume;

public class YearbookPSPAElement extends YearbookImageElement implements Serializable {
	private static final long serialVersionUID = -1279859963196943365L;
	
	public YearbookTextElement text;
	public Person person;
	public Point margins;

	public YearbookPSPAElement(Display display, String fileName, int pageWidth, int pageHeight) {
		super(display, fileName, pageWidth, pageHeight);
		text = new YearbookTextElement(pageWidth, pageHeight);
	}

	public YearbookPSPAElement(Display display, ImageData imageData, int pageWidth, int pageHeight) {
		super(display, imageData, pageWidth, pageHeight);
		text = new YearbookTextElement(pageWidth, pageHeight);
	}
	
	public YearbookPSPAElement(Display display, String fileName, int pageWidth, int pageHeight, Volume volume) {
		this(display, fileName, pageWidth, pageHeight);
		this.text = volume.textElement;
		this.computeBounds(volume, pageWidth, pageHeight);
	}

	private void computeBounds(Volume volume, int pageWidth, int pageHeight) {
		margins = Volume.photoSpacing(volume.grid, pageWidth, pageHeight);
		int width = margins.x * (volume.grid.x - 1);
		this.setScaleRelative((double) 1.4 * width / pageWidth);
		
	}
	
	@Override
	public boolean isPSPA() {
		return true;
	}

}
