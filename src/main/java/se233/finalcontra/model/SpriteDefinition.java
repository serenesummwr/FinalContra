package se233.finalcontra.model;

import javafx.scene.image.Image;

/**
 * Immutable metadata describing a sprite sheet along with timing configuration.
 */
public final class SpriteDefinition {
	private final Image image;
	private final int frameCount;
	private final int columns;
	private final int rows;
	private final int frameWidth;
	private final int frameHeight;
	private final int offsetX;
	private final int offsetY;
	private final long frameDurationNanos;

	public SpriteDefinition(Image image, int frameCount, int columns, int rows, int frameWidth, int frameHeight,
			int offsetX, int offsetY, long frameDurationNanos) {
		this.image = image;
		this.frameCount = frameCount;
		this.columns = columns;
		this.rows = rows;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.frameDurationNanos = frameDurationNanos;
	}

	public Image image() {
		return image;
	}

	public int frameCount() {
		return frameCount;
	}

	public int columns() {
		return columns;
	}

	public int rows() {
		return rows;
	}

	public int frameWidth() {
		return frameWidth;
	}

	public int frameHeight() {
		return frameHeight;
	}

	public int offsetX() {
		return offsetX;
	}

	public int offsetY() {
		return offsetY;
	}

	public long frameDurationNanos() {
		return frameDurationNanos;
	}

	public SpriteDefinition withFrameDuration(long newFrameDurationNanos) {
		return new SpriteDefinition(image, frameCount, columns, rows, frameWidth, frameHeight, offsetX, offsetY,
				newFrameDurationNanos);
	}
}