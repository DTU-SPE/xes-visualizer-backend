package dtu.spe.xesvisualizer.storage;

public class EmptyFileException extends RuntimeException {

	private static final long serialVersionUID = 3036632359507115157L;

	public EmptyFileException(String message) {
		super(message);
	}

	public EmptyFileException(String message, Throwable cause) {
		super(message, cause);
	}
}
