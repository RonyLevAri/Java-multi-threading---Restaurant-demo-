package blExceptions;

public class NoAvailableWaitersException extends Exception {

	public NoAvailableWaitersException() {

	}

	public NoAvailableWaitersException(String message) {
		super(message);
	}

	public NoAvailableWaitersException(Throwable cause) {
		super(cause);
	}
	
	public NoAvailableWaitersException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoAvailableWaitersException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
