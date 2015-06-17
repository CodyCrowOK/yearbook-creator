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
import java.util.ArrayList;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
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

	public static void exportToPDF(Yearbook yearbook, String fileName, Display display) throws IOException, COSVisitorException {
		ArrayList<java.awt.image.BufferedImage> images = new ArrayList<java.awt.image.BufferedImage>();
		for (int i = 0; i < yearbook.size(); i++) {
			Image image = new Image(display, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			GC gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			images.add(SWTUtils.convertToAWT(image.getImageData()));
			gc.dispose();
			image.dispose();
		}
		
		PDDocument document = new PDDocument();
		
		for (java.awt.image.BufferedImage image : images) {
			PDPage page = new PDPage();
			document.addPage(page);
			
			//Images will be too large normally, so we need to scale them.
			int newWidth = (int) page.getMediaBox().getWidth();
			int newHeight = (int) page.getMediaBox().getHeight();
			
			
			BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
			Graphics g = newImage.getGraphics();
			g.drawImage(image, 0, 0, newWidth, newHeight, null);
			g.dispose();
			
			PDPixelMap jpeg = new PDPixelMap(document, newImage);
			PDPageContentStream stream = new PDPageContentStream(document, page);
			stream.drawImage(jpeg, 0, 0);
			stream.close();
			
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
}
