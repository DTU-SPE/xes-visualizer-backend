package dtu.spe.xesvisualizer.storage;

public class CouldNotParseException extends RuntimeException {

	private static final long serialVersionUID = 705191751376652159L;

	public CouldNotParseException(String message) {
		super(message);
	}

	public CouldNotParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
