package se233.finalcontra.exception;

/**
 * Wraps {@link InterruptedException} instances raised while the game threads
 * sleep so they can be handled uniformly at the loop entry point.
 */
public class LoopInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LoopInterruptedException(String message, InterruptedException cause) {
		super(message, cause);
	}
}
