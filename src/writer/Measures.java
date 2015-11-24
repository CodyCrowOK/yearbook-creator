package writer;

public class Measures {

	public static int inchesToPixels(double inches, int pageWidth, double pageWidthInInches) {
		return (int) (pageWidth * inchesToPercent(inches, pageWidthInInches));
	}
	
	public static double inchesToPercent(double inches, double pageWidthInInches) {
		return inches / pageWidthInInches;
	}
	
	public static int percentToPixels(double percent, int pixels) {
		return (int) (percent * pixels);
	}
	
	public static double percentToInches(double percent, double pageWidthInInches) {
		return percent * pageWidthInInches;
	}
	
}
