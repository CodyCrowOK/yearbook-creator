package pdf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import writer.Creator;
import writer.SWTUtils;
import writer.UserSettings;
import writer.Yearbook;
import writer.YearbookElement;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFUtils {
	
	/**
	 * Converts a Deque of SWT ImageData (pages) into a PDF file.
	 * D
	 * <em>Note: If this is touched, it will break.</em>
	 * @param path file path of the new PDF
	 * @param images Deque of pages as SWT ImageData
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void SWTImagesToPDF(String path, Deque<ImageData> images) throws DocumentException, IOException {
		//Document tmpDoc = new Document();
		//Document document = new Document(new Rectangle((float) tmpDoc.getPageSize().getWidth(), (float) tmpDoc.getPageSize().getHeight()));
		Document document = new Document();
		document.setMargins(0, 0, 0, 0);
		FileOutputStream os = new FileOutputStream(path);
		PdfWriter writer = PdfWriter.getInstance(document, os);
		document.addAuthor("Cody Crow");
		writer.open();
		document.open();
		while (!images.isEmpty()) {
			ImageData imageData = images.pop();
			BufferedImage image = SWTUtils.convertToAWT(imageData);
			imageData = null;
			//BufferedImage scaledImage = resizeAWTImage(image, (int) document.getPageSize().getWidth(), (int) document.getPageSize().getHeight());
			//image = null;
			
			Image large = Image.getInstance(image, null);
			large.scaleToFit(document.getPageSize());

			document.add(large);
			if (!images.isEmpty()) document.newPage();
			image = null;
		}
		document.close();
		writer.close();
	}

	public static BufferedImage resizeAWTImage(BufferedImage img, int newW, int newH) { 
		java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
	
	public static void convertYearbookToPDF(String path, Yearbook yearbook, Display display) throws DocumentException, IOException {
		Document document = new Document();
		document.setMargins(0, 0, 0, 0);
		FileOutputStream os = new FileOutputStream(path);
		PdfWriter writer = PdfWriter.getInstance(document, os);
		document.addAuthor("Cody Crow");
		writer.open();
		document.open();
		
		
		ArrayList<YearbookElement> dummyList = new ArrayList<YearbookElement>();
		UserSettings dummySettings = new UserSettings();
		
		for (int i = 0; i < yearbook.size(); i++) {
			org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(display, yearbook.settings.publishWidth(), yearbook.settings.publishHeight());
			GC gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, dummyList, null, dummySettings, i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight(), true);
			gc.dispose();

			ImageData imageData = image.getImageData();
			image.dispose();
			BufferedImage bi = SWTUtils.convertToAWT(imageData);

			Image large = Image.getInstance(bi, null);
			large.scaleToFit(document.getPageSize());

			document.add(large);
			if (i < yearbook.size() - 1) document.newPage();
			
		}
		document.close();
		writer.close();
		
	}
}
