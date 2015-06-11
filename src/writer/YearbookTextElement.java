package writer;
import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/**
 * A textual element
 * @author Cody Crow
 *
 */
public class YearbookTextElement extends YearbookElement implements Serializable {
	private static final long serialVersionUID = 6972127757271364075L;
	
	public String text;
	public String fontFamily;
	public int size; //In points
	public boolean bold;
	public boolean italic;
	public boolean underline;
	
	private RGB rgb;
	private Font font;
	private Color color;
	private int pageWidth;
	private int pageHeight;
	
	private double width;
	private double height;

	public YearbookTextElement(int pageWidth, int pageHeight) {
		generateRandomElementId();
		this.x = 0;
		this.y = 0;
		this.rotation = 0;
		this.rgb = new RGB(0, 0, 0);
		this.text = "";
		this.size = 12;
		this.fontFamily = "Arial";
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
	}
	
	public YearbookTextElement(int x, int y, int pageWidth, int pageHeight) {
		this(pageWidth, pageHeight);
		this.x = ((double) x / pageWidth);
		this.y = ((double) y / pageHeight);
	}
	
	@Override
	public Rectangle getBounds() {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		int width = (int) (this.width * pageWidth);
		int height = (int) (this.height * pageHeight);
		return new Rectangle(x, y, width, height);
	}
	@Override
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		int width = (int) (this.width * pageWidth);
		int height = (int) (this.height * pageHeight);
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * This merely informs the text element of its bounds. It does NOT
	 * force the text to conform to it.
	 * @param r the new bounds
	 */
	public void setBounds(Rectangle r) {
		this.x = (double) r.x / pageWidth;
		this.y = (double) r.y / pageHeight;
		this.width = (double) r.width / pageWidth;
		this.height = (double) r.height / pageHeight;
	}
	
	@Override
	boolean isAtPoint(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	boolean isAtPoint(int x, int y, int pageWidth, int pageHeight) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setLocationRelative(int x, int y) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resize(Display display, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	public Color getColor(Device device) {
		if (color != null) color.dispose();
		color = new Color(device, rgb);
		return color;
	}
	
	public Font getFont(Device d) {
		if (font != null) font.dispose();
		font = new Font(d, this.fontFamily, this.size, this.fontStyle());
		return font;
	}
	
	public int fontStyle() {
		int style = SWT.NORMAL;
		if (bold) style |= SWT.BOLD;
		if (italic) style |= SWT.ITALIC;
		return style;
	}

	public boolean isText() {
		return true;
	}
	
	public void setRGB(RGB rgb) {
		this.rgb = rgb;
	}
	
	public void toggleBold() {
		this.bold = !this.bold;
	}
	
	public void toggleItalic() {
		this.italic = !this.italic;
	}
	
	public void toggleUnderline() {
		this.underline = !this.underline;
	}
}
