package writer;

/**
 * Used for logging during development. Set log to false for deployment.
 * @author Cody Crow
 *
 */
public class Logger {
	public static final boolean log = true;
	
	public static void println(String s) {
		if (log) System.out.println(s);
	}
	
	public static void printStackTrace(Throwable t) {
		if (log) t.printStackTrace();
	}
}
