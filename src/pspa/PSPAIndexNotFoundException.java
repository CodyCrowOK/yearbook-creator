package pspa;

/**
 * Thrown when there is no index.txt or master.txt file present.
 * @author Cody Crow
 *
 */
public class PSPAIndexNotFoundException extends Exception {

	private static final long serialVersionUID = 8575681199891038797L;

	public PSPAIndexNotFoundException() {
	}

	public PSPAIndexNotFoundException(String message) {
		super(message);
	}

	public PSPAIndexNotFoundException(Throwable cause) {
		super(cause);
	}

	public PSPAIndexNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PSPAIndexNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
