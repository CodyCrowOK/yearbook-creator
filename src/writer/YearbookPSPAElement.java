package writer;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import pspa.Person;
import pspa.Volume;

public class YearbookPSPAElement extends YearbookImageElement implements Serializable, Clickable {
	private static final long serialVersionUID = -1279859963196943365L;
	
	public YearbookTextElement text;
	public Person person;
	public Point margins;
	public Translation textTranslation;
	public boolean nameReversed;
	public boolean useTwoLinesForName;
	public Deque<Video> videos;

	public YearbookPSPAElement(Display display, String fileName, int pageWidth, int pageHeight) {
		super(display, fileName, pageWidth, pageHeight);
		videos = new ArrayDeque<Video>();
		text = new YearbookTextElement(pageWidth, pageHeight);
		textTranslation = new Translation();
	}

	public YearbookPSPAElement(Display display, ImageData imageData, int pageWidth, int pageHeight) {
		super(display, imageData, pageWidth, pageHeight);
		videos = new ArrayDeque<Video>();
		text = new YearbookTextElement(pageWidth, pageHeight);
		textTranslation = new Translation();
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
	
	@Override
	public boolean isTruePSPA() {
		return true;
	}

	@Override
	public Deque<Video> getVideos() {
		return videos;
	}
	
	@Override
	public boolean isClickable() {
		return videos.size() > 0;
	}

}
