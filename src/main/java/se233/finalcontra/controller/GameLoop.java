package se233.finalcontra.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.image.Image;
import se233.finalcontra.Launcher;
import se233.finalcontra.exception.GameStateException;
import se233.finalcontra.exception.InvalidSpriteException;
import se233.finalcontra.exception.ResourceLoadException;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.SpriteDefinition;
import se233.finalcontra.model.Enums.PlayerState;
import se233.finalcontra.model.Enums.ShootingDirection;
import se233.finalcontra.view.PauseMenu;
import se233.finalcontra.view.GameStages.GameStage;

public class GameLoop implements Runnable {
	public static ShootingDirection shootingDir;
	public static final Logger logger = LogManager.getLogger(GameLoop.class);

	private GameStage gameStage;
	private static PauseMenu pauseMenu;
	private int frameRate;
	private float interval;
	private boolean running;
	private static int score;

	public static boolean isPaused = false;
	public static boolean canPlayProneSound = true;

	public static List<Bullet> bullets = new ArrayList<>();
	public static List<Enemy> enemies = new ArrayList<>();

	public GameLoop(GameStage gameStage) {
		if (gameStage == null) {
			throw new GameStateException("GameStage is null — cannot initialize GameLoop.");
		}

		score = 0;
		pauseMenu = new PauseMenu();
		pauseMenu.setVisible(false);
		gameStage.getChildren().add(pauseMenu);

		this.gameStage = gameStage;
		this.frameRate = 10;
		this.interval = 1000 / frameRate;
		GameLoop.shootingDir = ShootingDirection.RIGHT;
		this.running = true;
	}

	private void update(Player player) {
		if (player == null) {
			throw new GameStateException("Player object is null — cannot update game loop.");
		}

		boolean leftPressed = gameStage.getKeys().isPressed(player.getLeftKey());
		boolean rightPressed = gameStage.getKeys().isPressed(player.getRightKey());
		boolean upPressed = gameStage.getKeys().isPressed(player.getUpKey());
		boolean downPressed = gameStage.getKeys().isPressed(player.getDownKey());
		boolean jumpPressed = gameStage.getKeys().isPressed(player.getJumpKey());

		if (isPaused || player.getState() == PlayerState.CHARGING || player.getState() == PlayerState.DIE) {
			return;
		}

		boolean horizontalInput = leftPressed ^ rightPressed;
		player.updateHorizontalMovement(horizontalInput);

		if (leftPressed && rightPressed) {
			player.stop();
			player.setProning(false);
			player.setState(PlayerState.IDLE);
		} else if (leftPressed) {
			player.moveLeft();
			shootingDir = ShootingDirection.LEFT;
			player.setProning(false);
			player.setState(PlayerState.WALKSHOOT);
			player.clearRunFrameHold();
		} else if (rightPressed) {
			player.moveRight();
			shootingDir = ShootingDirection.RIGHT;
			player.setProning(false);
			player.setState(PlayerState.WALKSHOOT);
			player.clearRunFrameHold();
		} else if (downPressed) {
			player.prone();
			tracePlayerAction("Prone");
			if (canPlayProneSound) {
				SoundController.getInstance().playProneSound();
				canPlayProneSound = false;
			}
			player.setState(PlayerState.PRONE);
			player.clearRunFrameHold();
			player.cancelShootingHold();
		} else {
			player.setProning(false);
			player.stop();
			player.setState(PlayerState.IDLE);
		}

		// Facing Direction
		if (upPressed && !rightPressed && !leftPressed) {
			shootingDir = ShootingDirection.UP;
			tracePlayerAction("Facing up");
			player.setState(PlayerState.FACE_UP);
			player.clearRunFrameHold();
		} else if (upPressed && rightPressed) {
			shootingDir = ShootingDirection.UP_RIGHT;
			tracePlayerAction("Facing up right");
			player.setState(PlayerState.FACE_UP_SIDE);
			player.clearRunFrameHold();
		} else if (downPressed && rightPressed) {
			shootingDir = ShootingDirection.DOWN_RIGHT;
			tracePlayerAction("Facing down right");
			player.setState(PlayerState.FACE_DOWN_SIDE);
			player.clearRunFrameHold();
		} else if (upPressed && leftPressed) {
			shootingDir = ShootingDirection.UP_LEFT;
			tracePlayerAction("Facing up left");
			player.setState(PlayerState.FACE_UP_SIDE);
			player.clearRunFrameHold();
		} else if (downPressed && leftPressed) {
			shootingDir = ShootingDirection.DOWN_LEFT;
			tracePlayerAction("Facing down left");
			player.setState(PlayerState.FACE_DOWN_SIDE);
			player.clearRunFrameHold();
		} else {
			shootingDir = shootingDir.toString().matches(".*RIGHT") ? ShootingDirection.RIGHT : ShootingDirection.LEFT;
			PlayerState currentState = player.getState();
			if (currentState != PlayerState.PRONE && currentState != PlayerState.WALKSHOOT) {
				player.setState(PlayerState.IDLE);
			}
		}

		if (player.getState() == PlayerState.IDLE && player.hasJustStoppedMovingHorizontally()) {
			player.latchRunFrameForIdle();
		}

		if (gameStage.getKeys().isJustPressed(player.getSwitchBulletKey())) {
			player.toggleLaserMode();
			String mode = player.isLaserMode() ? "LASER" : "NORMAL";
			logger.debug("Player bullet mode switched to {}", mode);
		}

		if (gameStage.getKeys().isJustPressed(player.getShootKey())) {
			player.shoot(gameStage, shootingDir);
			if (player.isProning()) {
				player.setState(PlayerState.PRONE);
				player.cancelShootingHold();
			} else {
				SpriteDefinition holdDefinition;
				PlayerState holdPose;
				switch (shootingDir) {
					case UP -> {
						holdDefinition = ImageAssets.PLAYER_SHOOT_UP;
						holdPose = PlayerState.FACE_UP;
					}
					case UP_RIGHT, UP_LEFT -> {
						holdDefinition = ImageAssets.PLAYER_SHOOT_UP_SIDE;
						holdPose = PlayerState.FACE_UP_SIDE;
					}
					case DOWN_RIGHT, DOWN_LEFT -> {
						holdDefinition = ImageAssets.PLAYER_FACE_DOWN_SIDE;
						holdPose = PlayerState.FACE_DOWN_SIDE;
					}
					default -> {
						holdDefinition = ImageAssets.PLAYER_WALK_SHOOT;
						holdPose = (leftPressed || rightPressed) ? PlayerState.WALKSHOOT : PlayerState.SHOOTING;
					}
				}
				if (holdDefinition == null) {
					throw new InvalidSpriteException("HoldDefinition sprite is missing for shooting direction: " + shootingDir);
				}
				player.beginShootingHold(holdDefinition);
				player.setState(holdPose);
				player.clearRunFrameHold();
			}
			tracePlayerAction("Shoot");
		}

		if (jumpPressed && downPressed) {
			player.dropDown();
			tracePlayerAction("Drop from platform");
			player.cancelShootingHold();
			player.clearRunFrameHold();
		} else if (jumpPressed) {
			player.jump();
			tracePlayerAction("Jump");
			player.cancelShootingHold();
			player.clearRunFrameHold();
		}

		canPlayProneSound = !player.isProning();
		gameStage.getKeys().clear();
	}

	public void updateAnimation(Player player) {
		if (player == null) {
			throw new GameStateException("Player is null in updateAnimation.");
		}

		var sprite = player.getImageView();
		if (sprite == null) {
			throw new InvalidSpriteException("Player sprite is null.");
		}

		PlayerState currentState = player.getState();

		if (player.isDying()) {
			if (ImageAssets.PLAYER_DIE == null) {
				throw new InvalidSpriteException("PLAYER_DIE sprite missing in ImageAssets.");
			}
			sprite.changeSpriteSheet(ImageAssets.PLAYER_DIE);
			sprite.tick();
			player.clearRunFrameHold();
			player.cancelShootingHold();
			return;
		}

		if (currentState == PlayerState.CHARGING) {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_CHARGING);
			sprite.tick();
			player.clearRunFrameHold();
			return;
		}

		if (player.isJumping() || player.isFalling()) {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_JUMP);
			sprite.tick();
			player.clearRunFrameHold();
			player.cancelShootingHold();
			return;
		}

		if (currentState == PlayerState.PRONE) {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_PRONE);
			player.cancelShootingHold();
			player.clearRunFrameHold();
			return;
		}

		if (player.isShootingHoldActive()) {
			SpriteDefinition holdDefinition = player.getShootingHoldDefinition();
			if (holdDefinition == null) {
				throw new InvalidSpriteException("Hold definition missing during shooting hold.");
			}
			sprite.changeSpriteSheet(holdDefinition);
			if (player.shouldAdvanceShootingAnimation() && sprite.getFrameCount() > 0) {
				sprite.tick();
			}
			if (player.shouldReleaseShootingHold()) {
				player.finishShootingHold();
			} else {
				return;
			}
		}

		switch (currentState) {
			case FACE_DOWN_SIDE -> sprite.changeSpriteSheet(ImageAssets.PLAYER_FACE_DOWN_SIDE);
			case FACE_UP_SIDE -> sprite.changeSpriteSheet(ImageAssets.PLAYER_SHOOT_UP_SIDE);
			case FACE_UP -> sprite.changeSpriteSheet(ImageAssets.PLAYER_SHOOT_UP);
			case WALKSHOOT -> sprite.changeSpriteSheet(ImageAssets.PLAYER_RUN);
			case SHOOTING -> sprite.changeSpriteSheet(ImageAssets.PLAYER_WALK_SHOOT);
			default -> sprite.changeSpriteSheet(ImageAssets.PLAYER_IDLE);
		}
		sprite.tick();
	}

	public void updateEnemyAnimation(Enemy enemy) {
		if (enemy == null) {
			throw new GameStateException("Enemy reference is null during animation update.");
		}
		if (enemy.getSprite() == null) {
			throw new InvalidSpriteException("Enemy sprite is null.");
		}

		if (enemy.getType() == EnemyType.JAVAHEAD) {
			if (!enemy.isAlive()) {
				enemy.getSprite().changeSpriteSheet(ImageAssets.DESTROYED_JAVA, 1, 1, 1);
			}
		}

		enemy.getSprite().tick();
	}

	public Image getimage(String path) {
		try {
			Image img = new Image(Launcher.class.getResourceAsStream(path));
			if (img.isError()) {
				throw new ResourceLoadException("Failed to load image: " + path);
			}
			return img;
		} catch (Exception e) {
			throw new ResourceLoadException("Error loading resource: " + path);
		}
	}

	public static void addScore(int addition) {
		score += addition;
		traceScore(addition);
	}

	public static int getScore() {
		return score;
	}

	public void stop() {
		running = false;
	}

	public static void pause() {
		isPaused = !isPaused;
		pauseMenu.setVisible(isPaused);
	}

	public static void traceScore(int addition) {
		logger.info("Score: {}+. Current score = {}", addition, score);
	}

	public static void tracePlayerAction(String action) {
		logger.debug("Player {}", action);
	}

	@Override
	public void run() {
		while (running) {
			float startTime = System.currentTimeMillis();
			try {
				Platform.runLater(() -> {
					if (isPaused) return;
					update(gameStage.getPlayer());
					updateAnimation(gameStage.getPlayer());
					for (Enemy e : new ArrayList<>(gameStage.getEnemies())) {
						updateEnemyAnimation(e);
					}
				});
			} catch (GameStateException | InvalidSpriteException | ResourceLoadException e) {
				logger.error("GameLoop error: {}", e.getMessage());
				e.printStackTrace();
			}

			float elapsedTime = System.currentTimeMillis() - startTime;
			try {
				Thread.sleep((long) Math.max(1, interval - elapsedTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
