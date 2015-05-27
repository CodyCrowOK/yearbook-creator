import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;


public class Yearbook {
	private ArrayList<YearbookPage> pages;
	public Image defaultBackground;
	
	String name;
	YearbookSettings settings;
	int activePage;
	
	public Yearbook() {
		pages = new ArrayList<YearbookPage>();
		settings = new YearbookSettings();
		activePage = 0;
		name = "Untitled";
		defaultBackground = null;
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
	
	/**
	 * Saves a yearbook to a file readable by the digital yearbook software
	 * (i.e. what the end user sees).
	 * @param yearbook The Yearbook to export
	 * @param fileName The name of the file to export to.
	 * @throws IOException
	 */
	public static void export(String fileName, Creator creator) throws IOException {
		/*
		 * First, prepare the yearbook for writing.
		 */
		/*
                Image drawable = new Image(e.display, canvas.getBounds());
		GC gc = new GC(drawable);
		canvas.print(gc);
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] {drawable.getImageData()};
		loader.save("c:\\swt.png", SWT.IMAGE_PNG);
		drawable.dispose();
		gc.dispose();
		*/
		
		
		
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
			return;
		}
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		
		
	}
}
