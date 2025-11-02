package se233.finalcontra.exception;

/**
 * Thrown when a requested game asset (image, sound, etc.) cannot be located on
 * the classpath.
 */
public class MissingAssetException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MissingAssetException(String path) {
		super("Unable to locate asset: " + path);
	}
}
