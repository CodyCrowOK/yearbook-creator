package writer;

public class YearbookSettings {
	public int width;
	public int height;
	
	public YearbookSettings(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	public YearbookSettings() {}
	
	public int publishWidth() {
		return (int) (8.5 * 300);
	}
	
	public int publishHeight() {
		return 11 * 300;
	}
	
}
