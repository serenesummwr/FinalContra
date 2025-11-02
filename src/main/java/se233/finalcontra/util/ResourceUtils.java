package se233.finalcontra.util;

import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import se233.finalcontra.Launcher;
import se233.finalcontra.exception.MissingAssetException;

/**
 * Centralises resource lookup so missing assets throw a consistent exception.
 */
public final class ResourceUtils {
	private ResourceUtils() {
	}

	public static URL requireResource(String path) {
		URL url = Launcher.class.getResource(path);
		if (url == null) {
			throw new MissingAssetException(path);
		}
		return url;
	}

	public static InputStream requireResourceStream(String path) {
		InputStream stream = Launcher.class.getResourceAsStream(path);
		if (stream == null) {
			throw new MissingAssetException(path);
		}
		return stream;
	}

	public static Image loadImage(String path) {
		return new Image(requireResourceStream(path));
	}

	public static AudioClip loadAudioClip(String path) {
		return new AudioClip(requireResource(path).toString());
	}
}
