package writer;
import java.awt.Desktop;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.itextpdf.text.DocumentException;

import pdf.PDFUtils;
import pspa.Volume;


public class Yearbook implements Serializable {
	private static final long serialVersionUID = 4099869425438846538L;
	private ArrayList<YearbookPage> pages;
	public ArrayList<Volume> volumes;
	transient private Image coverImage;
	transient private ImageData coverImageData;
	public boolean noBackground;
	public boolean hasCover;
	public Translation pspaTranslation;

	public String name;
	public YearbookSettings settings;
	public int activePage;

	public YearbookPageNumberElement numbers;

	public Yearbook() {
		volumes = new ArrayList<Volume>();
		pages = new ArrayList<YearbookPage>();
		settings = new YearbookSettings();
		activePage = 0;
		name = "Untitled";
		numbers = new YearbookPageNumberElement(settings.width, settings.height);
		pspaTranslation = new Translation();
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
	
	public YearbookPage pageById(int id) {
		for (YearbookPage page : pages) {
			if (page.id == id) return page;
		}
		return null;
	}
	
	public void insertPage(YearbookPage page, int position) {
		pages.add(position, page);
	}

	public void removePage(int index) {
		pages.remove(index);
	}
	
	public void removePage(YearbookPage page) {
		pages.remove(page);
	}
	
	public void removePages(int[] indices) {
		Deque<YearbookPage> pages = new ArrayDeque<YearbookPage>();
		for (int i : indices) {
			pages.add(this.pages.get(i));
		}
		for (YearbookPage p : pages) {
			this.pages.removeAll(pages);
		}
	}

	public int size() {
		return pages.size();
	}

	public Image cover(Display display) {
		//Try not to leak too many resources...
		if (display == null || this.coverImageData == null) return null; 
		if (this.coverImage != null && !this.coverImage.isDisposed()) {
			if (this.coverImage.getImageData() != this.coverImageData) {
			}
		} else {
			this.coverImage = new Image(display, this.coverImageData);
		}
		return this.coverImage;
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
	
	public void swapElement(YearbookElement elementOld, YearbookElement elementNew) {
		for (YearbookPage page : pages) {
			page.swapElement(elementOld, elementNew);
		}
	}
	
	public void setCover(ImageData data) {
		this.coverImageData = data;
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

			return yearbook;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static BufferedImage convertDoubleSpreadPDFToImage(String fileName) throws IOException {
		PDDocument document = PDDocument.loadNonSeq(new File(fileName), null);
		List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
		BufferedImage bi = pdPages.get(0).convertToImage(BufferedImage.TYPE_INT_RGB, 300);
		document.close();
		return bi;
	}

	public static void exportToPDF(Yearbook yearbook, String fileName, Display display) throws IOException, COSVisitorException, DocumentException {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PDFUtils.convertYearbookToPDF(fileName, yearbook, display);
				} catch (DocumentException | IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		//PDFUtils.convertYearbookToPDF(fileName, yearbook, display);
	}
	
	public static void previewPDF(Yearbook yearbook, Display display) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				String fileName = Creator.RESOURCE_DIR + File.separator + "~" + Integer.toString((int) (Math.random() * 1000000)) + "-tmp.pdf";
				
				try {
					PDFUtils.convertYearbookToPDF(fileName, yearbook, display);
					Desktop dt = Desktop.getDesktop();
					dt.open(new File(fileName));
					Thread.sleep(2000);
						
					Files.delete(new File(fileName).toPath());
					
				} catch (DocumentException | IOException | InterruptedException e) {
					Logger.printStackTrace(e);
				}
			}
			
		});
	}
	
	public static void exportToPNG(Yearbook yearbook, String folderName, Display display) throws IOException {
		Shell wait = new Shell(display);
		wait.setSize(300, 300);
		
		wait.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(
					PaintEvent e) {
				Font font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
				e.gc.setFont(font);
				int x = (300 - e.gc.textExtent("Please wait.").x) / 2;
				int y = (300 - e.gc.textExtent("Please wait.").y) / 2;
				e.gc.drawText("Please wait.", x, y, true);
				font.dispose();
			}
			
		});
		
		wait.open();
		
		ArrayList<YearbookElement> dummyList = new ArrayList<YearbookElement>();
		UserSettings dummySettings = new UserSettings();
		for (int i = 0; i < yearbook.size(); i++) {
			String str = "Page " + (i + 1) + " of " + yearbook.size();
			GC gc1 = new GC(wait);
			gc1.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc1.fillRectangle(new Rectangle(0, 0, 300, 300));
			Font font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
			gc1.setFont(font);
			int x = (300 - gc1.textExtent(str).x) / 2;
			int y = (300 - gc1.textExtent(str).y) / 2;
			gc1.drawText(str, x, y, true);
			font.dispose();
			gc1.dispose();
			
			org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(display, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			GC gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, dummyList, null, dummySettings, i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight(), true, true);
			gc.dispose();
			
			BufferedImage bi = SWTUtils.convertToAWT(image.getImageData());
			image.dispose();
			File output = new File(folderName + "/page-" + i + ".png");
			ImageIO.write(bi, "png", output);
		}
		
		wait.close();
		wait.dispose();
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
		if (this.coverImageData == null) {
			this.coverImageData = YearbookImages.bogusBackgroundData();
			this.noBackground = true;
		}
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { this.coverImageData };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageLoader.save(stream, SWT.IMAGE_PNG);
		byte[] bytes = stream.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	public ArrayList<YearbookPage> getPages() {
		return pages;
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
		this.coverImageData = data[0];
		if (this.noBackground) {
			this.coverImageData = null;
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
