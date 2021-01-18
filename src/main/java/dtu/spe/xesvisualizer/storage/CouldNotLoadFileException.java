package dtu.spe.xesvisualizer.storage;

public class CouldNotLoadFileException extends RuntimeException {

	private static final long serialVersionUID = -2620552801932620536L;

	public CouldNotLoadFileException(String message) {
		super(message);
	}

	public CouldNotLoadFileException(String message, Throwable cause) {
		super(message, cause);
	}

}
