package se233.finalcontra.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import se233.finalcontra.util.ResourceUtils;

/**
 * Helper to load sprite sheets and normalise their frames for animation.
 */
public final class SpriteSheetFactory {
	private SpriteSheetFactory() {
	}

	public static SpriteDefinition loadBillSprite(String resourcePath, int targetFrameWidth, int targetFrameHeight,
			long frameDurationNanos) {
		Image original = ResourceUtils.loadImage(resourcePath);
		PixelReader reader = original.getPixelReader();
		if (reader == null) {
			throw new IllegalArgumentException("Unable to read sprite resource: " + resourcePath);
		}

		List<FrameBounds> frames = extractFrameBounds(reader, (int) original.getWidth(), (int) original.getHeight());
		if (frames.isEmpty()) {
			throw new IllegalStateException("Sprite sheet contains no visible frames: " + resourcePath);
		}

		WritableImage uniformSheet = new WritableImage(targetFrameWidth * frames.size(), targetFrameHeight);
		PixelWriter writer = uniformSheet.getPixelWriter();

		for (int index = 0; index < frames.size(); index++) {
			FrameBounds frame = frames.get(index);
			int frameWidth = frame.width();
			int frameHeight = frame.height();
			int destX = index * targetFrameWidth + (targetFrameWidth - frameWidth) / 2;
			int destY = targetFrameHeight - frameHeight;

			for (int x = 0; x < frameWidth; x++) {
				for (int y = 0; y < frameHeight; y++) {
					int argb = reader.getArgb(frame.startX() + x, frame.startY() + y);
					writer.setArgb(destX + x, destY + y, argb);
				}
			}
		}

		Image uniform = uniformSheet;
		int columns = frames.size();
		return new SpriteDefinition(uniform, frames.size(), columns, 1, targetFrameWidth, targetFrameHeight, 0, 0,
				frameDurationNanos);
	}

	private static List<FrameBounds> extractFrameBounds(PixelReader reader, int width, int height) {
		List<FrameBounds> frames = new ArrayList<>();
		boolean inFrame = false;
		int frameStart = -1;

		for (int x = 0; x < width; x++) {
			boolean transparent = isTransparentColumn(reader, x, height);
			boolean lastColumn = x == width - 1;

			if (!transparent && !inFrame) {
				inFrame = true;
				frameStart = x;
			}

			if (inFrame && (transparent || lastColumn)) {
				int frameEnd = transparent ? x - 1 : x;
				if (frameEnd >= frameStart) {
					frames.add(computeFrameBounds(reader, frameStart, frameEnd, height));
				}
				inFrame = false;
			}
		}

		return frames;
	}

	private static boolean isTransparentColumn(PixelReader reader, int x, int height) {
		for (int y = 0; y < height; y++) {
			int argb = reader.getArgb(x, y);
			if (((argb >>> 24) & 0xFF) != 0) {
				return false;
			}
		}
		return true;
	}

	private static FrameBounds computeFrameBounds(PixelReader reader, int startX, int endX, int height) {
		int top = height;
		int bottom = -1;

		for (int x = startX; x <= endX; x++) {
			for (int y = 0; y < height; y++) {
				int argb = reader.getArgb(x, y);
				if (((argb >>> 24) & 0xFF) != 0) {
					top = Math.min(top, y);
					bottom = Math.max(bottom, y);
				}
			}
		}

		if (bottom < top) {
			// Entire segment is transparent; fallback to zero-height rectangle to avoid crashes.
			top = 0;
			bottom = 0;
		}

		return new FrameBounds(startX, top, endX - startX + 1, bottom - top + 1);
	}

	private record FrameBounds(int startX, int startY, int width, int height) {
	}
}
