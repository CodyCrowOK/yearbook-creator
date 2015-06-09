package writer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;


public class Yearbook implements Serializable {
	private static final long serialVersionUID = 4099869425438846538L;
	private ArrayList<YearbookPage> pages;
	transient private Image defaultBackground;
	transient private ImageData defaultBackgroundData;
	boolean noBackground;
	
	String name;
	public YearbookSettings settings;
	public int activePage;
	
	public Yearbook() {
		pages = new ArrayList<YearbookPage>();
		settings = new YearbookSettings();
		activePage = 0;
		name = "Untitled";
	}
	
	public Yearbook(String name) {
		this();
		this.name = name;
		pages.add(new YearbookPage(name));
	}
	
	public YearbookPage page(int index) {
		return pages.get(index);
	}
	
	public void addPage(String name) {
		pages.add(new YearbookPage(name));
	}
	
	public void removePage(int index) {
		pages.remove(index);
	}
	
	public int size() {
		return pages.size();
	}
	
	public void movePage(int source, int destination) {
		if (destination > size()) return;
		YearbookPage page = pages.get(source);
		pages.remove(source);
		pages.add(destination, page);
	}
	
	@SuppressWarnings("unchecked")
	public static Yearbook importFromPDF(Display display, String fileName) {

		PDDocument document;
		try {
			document = PDDocument.loadNonSeq(new File(fileName), null);
			ArrayList<PDPage> pdPages = (ArrayList<PDPage>) document.getDocumentCatalog().getAllPages();
			int page = 0;
			ArrayList<java.awt.Image> awtImages = new ArrayList<java.awt.Image>();
			for (PDPage pdPage : pdPages) {
				awtImages.add(pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, 300));
			}
			document.close();
			
			//After converted to images.
			
			ArrayList<ImageData> imageData = new ArrayList<ImageData>();
			for (java.awt.Image awtImage : awtImages) {
				imageData.add(SWTUtils.convertAWTImageToSWT(awtImage));
			}
			
			ArrayList<Image> images = new ArrayList<Image>();
			for (ImageData data : imageData) {
				images.add(new Image(display, data));
			}
			
			Yearbook yearbook = new Yearbook();
			for (Image image : images) {
				yearbook.pages.add(new YearbookPage(image));
			}
			
			return yearbook;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Saves a yearbook to a file readable by the digital yearbook software
	 * (i.e. what the end user sees).
	 * @param fileName The name of the file to export to.
	 * @param yearbook The Yearbook to export
	 * @throws IOException
	 *
	
	public static void export(String fileName, Yearbook yearbook, Display display) throws IOException {
		/*
		 * First, prepare the yearbook for writing.
		 *
		DigitalYearbook digitalYearbook = new DigitalYearbook(yearbook.name);
		int pageHeight = yearbook.settings.publishHeight();
		int pageWidth = yearbook.settings.publishWidth();
		ArrayList<Image> generatedImages = new ArrayList<Image>();
		ArrayList<YearbookClickableElement> clickables = new ArrayList<YearbookClickableElement>();
		
		for (YearbookPage page : yearbook.pages) {
			Image image = new Image(display, pageWidth, pageHeight);
			GC gc = new GC(image);
			
			//Set the background image.
			if (page.backgroundImage(display) != null) {
				gc.drawImage(page.backgroundImage(display), 0, 0, page.backgroundImage(display).getBounds().width, page.backgroundImage(display).getBounds().height, 0, 0, image.getBounds().width, image.getBounds().height);
			}
			
			//Map the YearbookImageElements to images...
			ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
			for (int i = 0; i < page.getElements().size(); i++) {
				if (page.element(i).isImage()) {
					images.add((YearbookImageElement) page.element(i));
				}
			}
			//...and display them.
			for (YearbookImageElement element : images) {
				gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, element.getBounds(pageWidth, pageHeight).x, element.getBounds(pageWidth, pageHeight).y, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height);
			}
			
			gc.dispose();
			generatedImages.add(image);
			
			//Map them like we did before...
			for (YearbookElement e : page.getElements()) {
				if (e.isClickable()) clickables.add((YearbookClickableElement) e);
			}
			
			digitalYearbook.pages.add(new DigitalYearbookPage(image, clickables));
		}
		
		
		
		/*
                Image drawable = new Image(e.display, canvas.getBounds());
		GC gc = new GC(drawable);
		canvas.print(gc);
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] {drawable.getImageData()};
		loader.save("c:\\swt.png", SWT.IMAGE_PNG);
		drawable.dispose();
		gc.dispose();
		*
		
		
		
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
			return;
		}
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(digitalYearbook);
		oos.flush();
		oos.close();
		
		
	}*/
	
	/**
	 * Reads in a yearbook file from disk.
	 * @param fileName The yearbook file to be read in
	 * @return The yearbook written to disk
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Yearbook readFromDisk(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		Yearbook yearbook = (Yearbook) ois.readObject();
		ois.close();
		return yearbook;
	}

	/**
	 * Saves the given yearbook to disk.
	 * @param yearbook The yearbook to be persisted
	 * @param fileName The file name of the saved yearbook
	 * @throws IOException
	 */
	public static void saveToDisk(Yearbook yearbook, String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(yearbook);
		oos.flush();
		oos.close();
	}
	
	/*
	 * Serialization methods
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (this.defaultBackgroundData == null) {
			this.defaultBackgroundData = YearbookImages.bogusBackgroundData();
			this.noBackground = true;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { this.defaultBackgroundData };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageLoader.save(stream, SWT.IMAGE_PNG);
		byte[] bytes = stream.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		ImageLoader imageLoader = new ImageLoader();
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		ImageData[] data = imageLoader.load(stream);
		this.defaultBackgroundData = data[0];
		if (this.noBackground) {
			this.defaultBackgroundData = null;
		}
	}
}
