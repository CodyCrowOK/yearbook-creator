
public class YearbookSettings {
	public int dpi;
	//Inches
	public double width;
	public double height;
	
	public YearbookSettings() {
		dpi = 300;
		width = 8.5;
		height = 11;
	}
	
	public int xResolution() {
		return (int) Math.floor(width * dpi); 
	}
	
	public int yResolution() {
		return (int) Math.floor(height * dpi);
	}
}
