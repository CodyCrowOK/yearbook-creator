package writer;
import java.awt.Graphics;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.util.ImageIOUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.itextpdf.text.DocumentException;

import pdf.PDFUtils;
import pspa.Volume;


public class Yearbook implements Serializable {
	private static final long serialVersionUID = 4099869425438846538L;
	private ArrayList<YearbookPage> pages;
	public ArrayList<Volume> pspaVolumes;
	transient private Image defaultBackground;
	transient private ImageData defaultBackgroundData;
	public boolean noBackground;
	public boolean hasCover;

	public String name;
	public YearbookSettings settings;
	public int activePage;
	
	public YearbookPageNumberElement pageNumber;

	public Yearbook() {
		pspaVolumes = new ArrayList<Volume>();
		pages = new ArrayList<YearbookPage>();
		settings = new YearbookSettings();
		activePage = 0;
		name = "Untitled";
		pageNumber = new YearbookPageNumberElement(settings.width, settings.height);
	}

	public Yearbook(String name) {
		this();
		this.name = name;
		pages.add(new YearbookPage(name));
	}

	public YearbookPage page(int index) {
		if (index >= this.size()) {
			index = 0;
			this.activePage = 0;
		}
		return pages.get(index);
	}

	public void addPage(String name) {
		pages.add(new YearbookPage(name));
	}
	
	public void addPage(YearbookPage page) {
		pages.add(page);
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
	
	public void removeElement(YearbookElement element) {
		for (YearbookPage page : pages) {
			page.removeElement(element);
		}
	}

	@SuppressWarnings("unchecked")
	public static Yearbook importFromPDF(Display display, String fileName) {

		PDDocument document;
		try {
			document = PDDocument.loadNonSeq(new File(fileName), null);
			ArrayList<PDPage> pdPages = (ArrayList<PDPage>) document.getDocumentCatalog().getAllPages();
			ArrayList<java.awt.Image> awtImages = new ArrayList<java.awt.Image>();
			for (PDPage pdPage : pdPages) {
				awtImages.add(pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, 300));
			}
			document.close();

			//After converted to images.
			Yearbook yearbook = new Yearbook();
			for (java.awt.Image awtImage : awtImages) {
				yearbook.pages.add(new YearbookPage(SWTUtils.convertAWTImageToSWT(awtImage)));
			}

			/*
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
				image.dispose();
			}
			*/

			return yearbook;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void exportToPDF(Yearbook yearbook, String fileName, Display display) throws IOException, COSVisitorException, DocumentException {
		Deque<ImageData> list = new ArrayDeque<ImageData>();
		ArrayList<YearbookElement> dummyList = new ArrayList<YearbookElement>();
		UserSettings dummySettings = new UserSettings();
		for (int i = 0; i < yearbook.size(); i++) {
			Image image = new Image(display, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			GC gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, dummyList, null, dummySettings, i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight(), true);
			gc.dispose();
			System.out.println(list.size());
			list.add(image.getImageData());
			image.dispose();
		}
		
		PDFUtils.SWTImagesToPDF(fileName, list);
	}

	public static void exportToPDFOLD(Yearbook yearbook, String fileName, Display display) throws IOException, COSVisitorException {
		PDDocument document = new PDDocument();
		
		//PDPage page1 = new PDPage();
		//System.out.println(page1.getMediaBox());
		//if (!false) return;
		
		//ArrayList<java.awt.image.BufferedImage> images = new ArrayList<java.awt.image.BufferedImage>();
		for (int i = 0; i < yearbook.size(); i++) {
			Image image = new Image(display, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			GC gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight(), true);
			//images.add(SWTUtils.convertToAWT(image.getImageData()));
			PDRectangle rect = new PDRectangle((float) 2550, (float) 3300);
			PDPage page = new PDPage(rect);
			document.addPage(page);
			//ImageIOUtil.writeImage(SWTUtils.convertToAWT(image.getImageData()), "out-" + i + ".png", 300);
			
			//Images will be too large normally, so we need to scale them.
			//int newWidth = (int) page.getMediaBox().getWidth();
			//int newHeight = (int) page.getMediaBox().getHeight();
			/*int newWidth = imageAWT.getWidth();
			int newHeight = imageAWT.getHeight();
			
			
			BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
			Graphics g = newImage.getGraphics();
			g.drawImage(image, 0, 0, newWidth, newHeight, null);
			g.dispose();
			*/
			PDPixelMap jpeg = new PDPixelMap(document, SWTUtils.convertToAWT(image.getImageData()));
			PDPageContentStream stream = new PDPageContentStream(document, page);
			stream.drawImage(jpeg, 0, 0);
			stream.close();
			gc.dispose();
			image.dispose();
		}
		
		document.save(fileName);
		document.close();
	}

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

	/**
	 * Magic function which keeps a yearbook from turning into a fork bomb.
	 */
	public void tidyUp() {
		for (int i = 0; i < this.size(); i++) {
			if (Math.abs(this.activePage - i) > 2) this.page(i).setInactive();
		}
		
	}
}
