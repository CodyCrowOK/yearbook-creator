package pdf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;

import org.eclipse.swt.graphics.ImageData;

import writer.SWTUtils;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFUtils {
	
	/**
	 * Converts a Deque of SWT ImageData (pages) into a PDF file.
	 * 
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
		//tmpDoc = null;
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
}
