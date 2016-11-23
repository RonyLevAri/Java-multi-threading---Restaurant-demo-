package blExceptions;

public class NameException extends Exception {

	public NameException() {

	}

	public NameException(String message) {
		super(message);
	}

	public NameException(Throwable cause) {
		super(cause);
	}
	
	public NameException(String message, Throwable cause) {
		super(message, cause);
	}

	public NameException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
