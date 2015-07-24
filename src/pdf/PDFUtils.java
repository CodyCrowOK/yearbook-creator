/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package pdf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import writer.Creator;
import writer.SWTUtils;
import writer.UserSettings;
import writer.Yearbook;
import writer.YearbookElement;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * PDF utilities for SWT images and Digital Express Yearbook format.
 * License: AGPL v3
 * @author Cody Crow
 *
 */
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
		
		
		Document document = new Document();
		document.setMargins(0, 0, 0, 0);
		FileOutputStream os = new FileOutputStream(path);
		PdfWriter writer = PdfWriter.getInstance(document, os);
		document.addAuthor("Cody Crow");
		writer.open();
		document.open();
		
		
		ArrayList<YearbookElement> dummyList = new ArrayList<YearbookElement>();
		UserSettings dummySettings = new UserSettings();
		
		/*
		 * Do front cover
		 */
		
		org.eclipse.swt.graphics.Image front = new org.eclipse.swt.graphics.Image(display, (int) document.getPageSize().getWidth(), (int) document.getPageSize().getHeight());
		org.eclipse.swt.graphics.Image back = new org.eclipse.swt.graphics.Image(display, (int) document.getPageSize().getWidth(), (int) document.getPageSize().getHeight());

		org.eclipse.swt.graphics.Image cover = yearbook.cover(display);
		
		GC gc = new GC(front);
		gc.drawImage(cover, cover.getBounds().width / 2, 0, (int) Math.floor(cover.getBounds().width / 2), cover.getBounds().height, 0, 0, front.getBounds().width, front.getBounds().height);
		gc.dispose();
		
		gc = new GC(back);
		gc.drawImage(cover, 0, 0, (int) Math.floor(cover.getBounds().width / 2), cover.getBounds().height, 0, 0, back.getBounds().width, back.getBounds().height);
		gc.dispose();
		
		cover.dispose();
		if (yearbook.hasCover) {
			ImageData imageData = front.getImageData();
			BufferedImage bi = SWTUtils.convertToAWT(imageData);
			Image large = Image.getInstance(bi, null);
			large.scaleToFit(document.getPageSize());
			
			document.add(large);
			document.newPage();
		}
		front.dispose();
			
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
			gc = new GC(image);
			Creator.paintPage(gc, display, yearbook, dummyList, null, dummySettings, i, yearbook.settings.publishWidth(), yearbook.settings.publishHeight(), true, true);
			gc.dispose();

			ImageData imageData = image.getImageData();
			image.dispose();
			BufferedImage bi = SWTUtils.convertToAWT(imageData);
			Image large = Image.getInstance(bi, null);
			large.scaleToFit(document.getPageSize());

			document.add(large);
			if (i < yearbook.size() - 1 || yearbook.hasCover) document.newPage();
			
		}
		
		if (yearbook.hasCover) {
			ImageData imageData = back.getImageData();
			BufferedImage bi = SWTUtils.convertToAWT(imageData);
			Image large = Image.getInstance(bi, null);
			large.scaleToFit(document.getPageSize());
			
			document.add(large);
		}
		back.dispose();
		
		document.close();
		writer.close();
		
		wait.close();
		wait.dispose();
	}
}
