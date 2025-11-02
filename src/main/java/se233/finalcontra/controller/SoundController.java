package se233.finalcontra.controller;

import javafx.scene.media.AudioClip;
import se233.finalcontra.util.ResourceUtils;

public class SoundController {
	private static SoundController instance;
	private AudioClip shootSound;
	private AudioClip jumpSound;
	private AudioClip enemyDieSound;
	private AudioClip explosionSound;
	private AudioClip cannonSound;
	private AudioClip playerDieSound;
	private AudioClip proneSound;
	private AudioClip winSound;
	private AudioClip javaDieSound;
	private AudioClip javaAttackSound;
	private AudioClip startTheme;
	// Theme music used for all stages
	private AudioClip minecraftTheme;
	private AudioClip loseSound;
	private AudioClip respawnSound;
	private AudioClip laserSound;
	private AudioClip blockHitSound;

	private static AudioClip loadClip(String path) {
		// Let MissingAssetException propagate to the global handler in Launcher
		return ResourceUtils.loadAudioClip(path);
	}
	
    public void stopAllSounds() {
        if (shootSound != null) shootSound.stop();
        if (jumpSound != null) jumpSound.stop();
        if (enemyDieSound != null) enemyDieSound.stop();
        if (explosionSound != null) explosionSound.stop();
        if (cannonSound != null) cannonSound.stop();
        if (playerDieSound != null) playerDieSound.stop();
        if (proneSound != null) proneSound.stop();
        if (winSound != null) winSound.stop();
        if (javaDieSound != null) javaDieSound.stop();
        if (javaAttackSound != null) javaAttackSound.stop();
        if (startTheme != null) startTheme.stop();
		if (minecraftTheme != null) minecraftTheme.stop();
        if (loseSound != null) loseSound.stop();
        if (respawnSound != null) respawnSound.stop();
        if (laserSound != null) laserSound.stop();
		if (blockHitSound != null) blockHitSound.stop();
    }
	private SoundController() {
		shootSound = loadClip("assets/Sounds/gunshot.mp3");
		jumpSound = loadClip("assets/Sounds/jumpSound.mp3");
		enemyDieSound = loadClip("assets/Sounds/dieSound.mp3");
		explosionSound = loadClip("assets/Sounds/explosionSound.mp3");
		cannonSound = loadClip("assets/Sounds/cannonSound.mp3");
		playerDieSound = loadClip("assets/Sounds/playerDieSound.mp3");
		proneSound = loadClip("assets/Sounds/proneSound.mp3");
		winSound = loadClip("assets/Sounds/winSound.mp3");
		javaDieSound = loadClip("assets/Sounds/javaDieSound.mp3");
		javaAttackSound = loadClip("assets/Sounds/javaAttackSound.mp3");
		loseSound = loadClip("assets/Sounds/loseSound.mp3");
		respawnSound = loadClip("assets/Sounds/respawnSound.mp3");
		laserSound = loadClip("assets/Sounds/laser.mp3");
		// Global theme for all stages
		minecraftTheme = loadClip("assets/Sounds/minecraftSound.mp3");

		if (shootSound != null) shootSound.setVolume(0.25);
		if (laserSound != null) laserSound.setVolume(0.3);
		if (respawnSound != null) respawnSound.setVolume(0.15);
		if (jumpSound != null) jumpSound.setVolume(0.1);
		if (playerDieSound != null) playerDieSound.setVolume(0.35);
		if (blockHitSound != null) blockHitSound.setVolume(0.3);
		if (minecraftTheme != null) {
			minecraftTheme.setVolume(0.25);
			minecraftTheme.setCycleCount(AudioClip.INDEFINITE);
		}
	}

	
	public void playShootSound() {
		if (shootSound != null) shootSound.play();
	}
	public void playJumpSound() {
		if (jumpSound != null) jumpSound.play();
	}
	
	public void playDieSound() {
		if (enemyDieSound != null) enemyDieSound.play();
	}
	
	public void playExplosionSound() {
		if (explosionSound != null) explosionSound.play();
	}

	public void playCannonSound() {
		if (cannonSound != null) cannonSound.play();
	}
	
	public void playLaserSound() {
		if (laserSound != null) laserSound.play();
	}
	public void playPlayerDieSound() {
		if (playerDieSound != null) playerDieSound.play();
	}
	public void playProneSound() {
		if (proneSound != null) proneSound.play();
	}
	
	public void playWinSound() {
		if (winSound != null) winSound.play();
	}
	
	public void playJavaDieSound() {
		if (javaDieSound != null) javaDieSound.play();
	}
	
	public void playJavaAttackSound() {
		if (javaAttackSound != null) javaAttackSound.play();
	}
	
	public void playStartTheme() {
		if (startTheme != null) startTheme.play();
	}
	public void playLoseSound() {
		if (loseSound != null) loseSound.play();
	}
	
	public void playRespawnSound() {
		if (respawnSound != null) respawnSound.play();
	}

	// Block hit sound (replaces metal hit)
	public void playBlockHitSound() {
		if (blockHitSound != null) blockHitSound.play();
	}

	// Global stage theme controls
	public void playMinecraftTheme() {
		if (minecraftTheme != null) minecraftTheme.play();
	}

	public void stopMinecraftTheme() {
		if (minecraftTheme != null) minecraftTheme.stop();
	}
	public static SoundController getInstance() {
		if (instance == null) instance = new SoundController();
		return instance;
	}
}
