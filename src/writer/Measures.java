package writer;

public class Measures {

	public static int inchesToPixels(double inches, int pageWidth, double pageWidthInInches) {
		return (int) (pageWidth * inchesToPercent(inches, pageWidthInInches));
	}
	
	public static double inchesToPercent(double inches, double pageWidthInInches) {
		return inches / pageWidthInInches;
	}
	
}
