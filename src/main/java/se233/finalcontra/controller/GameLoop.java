package se233.finalcontra.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.image.Image;
import se233.finalcontra.exception.LoopInterruptedException;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enemy;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.model.ImageAssets;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.SpriteDefinition;
import se233.finalcontra.model.Enums.PlayerState;
import se233.finalcontra.model.Enums.ShootingDirection;
import se233.finalcontra.util.ResourceUtils;
import se233.finalcontra.view.PauseMenu;
import se233.finalcontra.view.GameStages.GameStage;

public class GameLoop implements Runnable{
	public static ShootingDirection shootingDir;
	public static final Logger logger = LogManager.getLogger(GameLoop.class);

	private GameStage gameStage;
	private static PauseMenu pauseMenu;
	private int frameRate;
	private float interval;
	private boolean running;
	private static int score;
	private static boolean resetScoreOnNextStart = true;
	
	public static boolean isPaused = false;
	public static boolean canPlayProneSound = true;

	public static List<Bullet> bullets = new ArrayList<Bullet>();
	public static List<Enemy> enemies = new ArrayList<Enemy>();
	public GameLoop(GameStage gameStage) {
		if (resetScoreOnNextStart) {
			score = 0;
			resetScoreOnNextStart = false;
		}
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
			// Set default direction while not pressing any key
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
		
		if (player.isProning()) {
			canPlayProneSound = false;
		} else {
			canPlayProneSound = true;
		}

		gameStage.getKeys().clear();
	}
	
	public void updateAnimation(Player player) {
		SpriteAnimation sprite = player.getImageView();
		PlayerState currentState = player.getState();

		if (player.isDying()) {
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
			if (holdDefinition != null) {
				sprite.changeSpriteSheet(holdDefinition);
			}
			if (player.shouldAdvanceShootingAnimation() && sprite.getFrameCount() > 0) {
				int before = sprite.getCurrentFrameIndex();
				sprite.tick();
				if (sprite.isAtLastFrame()) {
					player.markShootingAnimationCompleted();
				} else if (sprite.getCurrentFrameIndex() < before) {
					sprite.setFrameIndex(Math.max(0, sprite.getFrameCount() - 1));
					player.markShootingAnimationCompleted();
				}
			}
			if (player.shouldReleaseShootingHold()) {
				player.finishShootingHold();
			} else {
				return;
			}
		}

		switch (currentState) {
		case FACE_DOWN_SIDE -> {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_FACE_DOWN_SIDE);
			sprite.tick();
			player.clearRunFrameHold();
			return;
		}
		case FACE_UP_SIDE -> {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_SHOOT_UP_SIDE);
			sprite.tick();
			player.clearRunFrameHold();
			return;
		}
		case FACE_UP -> {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_SHOOT_UP);
			sprite.tick();
			player.clearRunFrameHold();
			return;
		}
		case WALKSHOOT -> {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_RUN);
			if (player.consumePendingRunResume()) {
				sprite.setFrameIndex(player.getLastRunFrameIndex());
			}
			sprite.tick();
			player.captureRunFrameIndex();
			return;
		}
		case SHOOTING -> {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_WALK_SHOOT);
			sprite.tick();
			player.clearRunFrameHold();
			return;
		}
		default -> {
		}
		}

		if (player.shouldHoldRunFrame() && player.hasRunHistory()) {
			sprite.changeSpriteSheet(ImageAssets.PLAYER_RUN);
			sprite.setFrameIndex(player.getLastRunFrameIndex());
			return;
		}

		sprite.changeSpriteSheet(ImageAssets.PLAYER_IDLE);
	}
	
	public void updateEnemyAnimation(Enemy enemy){

		if (enemy.getType() == EnemyType.JAVAHEAD){
			if (!enemy.isAlive()){
				enemy.getSprite().changeSpriteSheet(ImageAssets.DESTROYED_JAVA, 1, 1, 1);
			}
		}

		enemy.getSprite().tick();
	}

	public Image getimage(String path){
		return ResourceUtils.loadImage(path);
	}

	public static void addScore(int addition) {
		score += addition;
		traceScore(addition);
	}
	public static int getScore() { return score; }

	public static void prepareScoreReset() {
		resetScoreOnNextStart = true;
	}

	public static void resetScore() {
		score = 0;
		resetScoreOnNextStart = false;
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

	private void sleepForNextFrame(float elapsedTime) {
		long sleepDuration = calculateSleepDuration(elapsedTime);
		if (sleepDuration <= 0) {
			return;
		}
		try {
			Thread.sleep(sleepDuration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LoopInterruptedException("Game loop interrupted during sleep", e);
		}
	}

	private long calculateSleepDuration(float elapsedTime) {
		float remaining = elapsedTime < interval
				? interval - elapsedTime
				: interval - (interval % elapsedTime);
		return (long) Math.max(0, remaining);
	}


	@Override
	public void run() {
		try {
			while (running) {
				float startTime = System.currentTimeMillis();
				Platform.runLater(() -> {
					if (isPaused) {
						return;
					}
					update(gameStage.getPlayer());
					updateAnimation(gameStage.getPlayer());
					for (Enemy enemy : new ArrayList<>(gameStage.getEnemies())) {
						updateEnemyAnimation(enemy);
					}
				});

				float elapsedTime = System.currentTimeMillis() - startTime;
				sleepForNextFrame(elapsedTime);
			}
		} catch (LoopInterruptedException e) {
			logger.warn("Stopping game loop after interruption", e);
			running = false;
		}
	}
}
