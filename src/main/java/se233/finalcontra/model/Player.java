package se233.finalcontra.model;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.Launcher;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.controller.SpriteAnimation;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.BulletType;
import se233.finalcontra.model.Enums.PlayerState;
import se233.finalcontra.model.Enums.ShootingDirection;
import se233.finalcontra.view.Platform;
import se233.finalcontra.view.GameStages.GameStage;

public class Player extends Pane {
	private static final Logger logger = LogManager.getLogger(Player.class);
	
	private KeyCode leftKey;
	private KeyCode rightKey;
	private KeyCode upKey;
	private KeyCode downKey;
	private KeyCode shootKey;
	private KeyCode jumpKey;
	private KeyCode CheatKey;
	private KeyCode switchBulletKey;
	
	private int xPos;
	private int yPos;
	private int startX;
	private int startY;
	private double xVelocity = 0;
	private double xAcceleration = 1;
	private double yVelocity = 0;
	private double yAcceleration = 0.40d;
	private double xMaxVelocity = 5;
	private double yMaxVelocity = 10;
	
	private Rectangle hitBox;
	
	private int lives = 3;
	
	// Movement booleans
	private boolean isMoveRight = true;
	private boolean isMoveLeft = false;
	private boolean isJumping = false;
	private boolean isFalling = true;
	private boolean canJump = false;
	private boolean isProning = false;
	private boolean isOnPlatform = false;
	private boolean isDropping = false;
	private boolean canDropDown = false;
	private boolean isDying = false;
	private boolean CheatActive = true;
	private boolean isLaserMode = false;
	
	public static int height;
	public static int width;
	private SpriteAnimation sprite;
	private double spriteRightOffset;
	private double spriteLeftOffset;
	private boolean wasMovingHorizontally = false;
	private boolean isMovingHorizontally = false;
	private boolean holdRunFrame = false;
	private boolean pendingRunResume = false;
	private boolean hasWalkedOnce = false;
	private int lastRunFrameIndex = 0;

	private boolean shootingHoldActive = false;
	private boolean shootingAnimationCompleted = false;
	private long shootingHoldUntilNanos = 0;
	private SpriteDefinition shootingHoldDefinition;

	private static final long SHOOT_HOLD_DURATION_NANOS = TimeUnit.MILLISECONDS.toNanos(200);
	
	private long lastShotTime = 0;
	private int fireDelay = 30;
	private int bulletPerClip = 3;
	private int dropDownTimer = 0;
	private int reloadTimer = 0;
	public static int respawnTimer = 0;
	public static int spawnProtectionTimer = 200;
	
	private PlayerState playerState;
	private GameStage currentStage;
	
	public Player(int xPos, int yPos, KeyCode leftKey, KeyCode rightKey, KeyCode upKey, KeyCode downKey) {
		this.playerState = PlayerState.IDLE;
		this.bulletPerClip = 3;
		this.dropDownTimer = 0;
		this.reloadTimer = 0;
		respawnTimer = 0;
		this.startX = xPos;
		this.startY = yPos;
		this.leftKey = leftKey;
		this.rightKey = rightKey;
		this.upKey = upKey;
		this.downKey = downKey;
		this.shootKey = KeyCode.L;
		this.jumpKey = KeyCode.K;
		this.CheatKey = KeyCode.F1;
		this.switchBulletKey = KeyCode.X;
		this.xPos = xPos;
		this.yPos = yPos;
		Player.height = 64;
		Player.width = 64;
		this.hitBox = new Rectangle(width - 25, height);
		this.setTranslateX(xPos);
		this.setTranslateY(yPos);
		this.sprite = new SpriteAnimation(ImageAssets.PLAYER_IDLE);
		double displaySize = 128.0; // เพิ่มขนาดจาก 96 เป็น 128
		this.sprite.setFitWidth(displaySize);
		this.sprite.setFitHeight(displaySize);
		spriteRightOffset = (Player.width - displaySize) / 2.0;
		spriteLeftOffset = spriteRightOffset; // keep same offset - flip handles the mirroring
		double yOffset = Player.height - displaySize;
		this.sprite.setTranslateX(spriteRightOffset);
		this.sprite.setTranslateY(yOffset);
		this.hitBox.setFill(Color.TRANSPARENT);
		this.getChildren().addAll(sprite, hitBox);
		this.setWidth(width);
		this.setHeight(height);
	}
	
	//					Starting of Movement Behaviors
	public void moveLeft() {
		isMoveRight = false;
		isMoveLeft = true;
		sprite.setScaleX(-1);
		sprite.setTranslateX(spriteLeftOffset);
		
	}
	
	public void moveRight() {
		isMoveRight = true;
		isMoveLeft = false;
		sprite.setScaleX(1);
		sprite.setTranslateX(spriteRightOffset);
	}
	
	public void jump() {
		if (canJump) {
			SoundController.getInstance().playJumpSound();
			yVelocity = yMaxVelocity;
			canJump = false;
			isJumping = true;
			isFalling = false;
		}
	}
	
	public void respawn() {
		enableKeys();
		this.yPos = this.startY;
		this.xPos = this.startX;
		this.isMoveLeft = false;
		this.isMoveRight = false;
		this.isFalling = true;
		this.canJump = false;
		this.isJumping = false;
		this.isDying = false;
		this.setState(PlayerState.IDLE);
		refreshLivesLabel();
		SoundController.getInstance().playRespawnSound();
	}
	
	
	public void die() {
		respawnTimer = 100;
		spawnProtectionTimer = 200;
		lives--;
		isDying = true;
		this.setState(PlayerState.DIE);
		refreshLivesLabel();
		SoundController.getInstance().playPlayerDieSound();
		if (lives <= 0) {
			javafx.application.Platform.runLater(() -> {
				GameLoop.pause();
				ButtonType retry = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
				ButtonType quit = new ButtonType("Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
				Alert alert = new Alert(AlertType.CONFIRMATION, "Game Over!");
				alert.setTitle("Game Over!");
				alert.setHeaderText("Retry?");
				alert.getButtonTypes().setAll(retry, quit);
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == retry) {
					Launcher.changeStage(Launcher.currentStageIndex);
				} else {
					Launcher.exitToMenu();
				}
			});

			SoundController.getInstance().stopAllSounds();
			SoundController.getInstance().playPlayerDieSound();
			SoundController.getInstance().playLoseSound();
		}
	}
	
	public void setProning(boolean isProning) {
		this.isProning = isProning;
	}

	public void prone() {
		isProning = true;
		hitBox.setHeight(height/2);
		hitBox.setY(height/2);
		stop();
	}
	
	public void resetHitBoxHeight() {
		if (isProning == false) {
			hitBox.setHeight(height);
			hitBox.setY(0);
		}
	}
	
	public void dropDown() {
		if (isOnPlatform && canDropDown) {
			isDropping = true;
			yVelocity = 4;
			dropDownTimer = 12;
			isOnPlatform = false;
			canJump = false;
			isFalling = true;
			isJumping = false;
			canDropDown = false;
			SoundController.getInstance().playDropDownSound();
		}
	}
	
	public void shoot(GameStage gameStage, ShootingDirection direction) {
		if (reloadTimer > 0) {
			return;
		}

		int xBulletPos = switch (direction) {
			case UP -> xPos + (width / 2);
			case RIGHT -> xPos + width;
			case UP_RIGHT -> xPos + 8;
			case DOWN_RIGHT -> xPos + width - 5;
			case LEFT -> xPos;
			case UP_LEFT -> xPos + 8;
			case DOWN_LEFT -> xPos + 5;
		};

		int yBulletPos = switch (direction) {
			case RIGHT, LEFT -> yPos + 5;
			case UP_RIGHT, UP_LEFT -> yPos;
			case DOWN_RIGHT, DOWN_LEFT -> yPos + 15;
			case UP -> yPos - 10;
		};

		long now = System.currentTimeMillis();
		if (now - lastShotTime < fireDelay) {
			return;
		}

		lastShotTime = now;
		if (bulletPerClip > 0) {
			reloadTimer = 0;
			bulletPerClip--;
		} else {
			reloadTimer = 30;
			bulletPerClip = 3;
		}

		Bullet bullet;
		if (isLaserMode) {
			int laserStartY = isProning ? (yPos + height / 2) : yBulletPos;
			bullet = new Bullet(xBulletPos, laserStartY, 14, 14, direction, BulletOwner.PLAYER, BulletType.LASER);
			SoundController.getInstance().playLaserSound();
		} else {
			bullet = isProning
					? new Bullet(xBulletPos, (yPos + height / 2), 10, 10, direction, BulletOwner.PLAYER)
					: new Bullet(xBulletPos, yBulletPos, 10, 10, direction, BulletOwner.PLAYER);
			SoundController.getInstance().playShootSound();
		}

		GameLoop.bullets.add(bullet);
		javafx.application.Platform.runLater(() -> gameStage.getChildren().add(bullet));
	}
	
	public void updateTimer() {
		if (reloadTimer > 0) {
			reloadTimer--;
		}
		if (spawnProtectionTimer > 0) {
			spawnProtectionTimer--;
		}
	}
	
	public void stop() {
		
		isMoveLeft = false;
		isMoveRight = false;
		xVelocity = 0;
	}
	
	public void repaint() {
		moveY();
		if (respawnTimer > 0) {
			respawnTimer--;
			disableKeys();
			if (respawnTimer == 0) {
				respawn();
			}
			return;
		}
		isDying = false;
		enableKeys();
		moveX();
	}
	
	public void moveX() {
		setTranslateX(xPos);
		if (isMoveRight) {
			xVelocity = xVelocity >= xMaxVelocity ? xMaxVelocity : xVelocity + xAcceleration;
			xPos += xVelocity;
		} 
		if (isMoveLeft)	{
			xVelocity = xVelocity >= xMaxVelocity ? xMaxVelocity : xVelocity + xAcceleration;
			xPos -= xVelocity;
		}
	}
	
	public void moveY() {
		setTranslateY(yPos);
		if (isFalling) {
			yVelocity = yVelocity >= yMaxVelocity ? yMaxVelocity : yVelocity + yAcceleration;
			yPos += yVelocity;
		} else if (isJumping) {
			yVelocity = yVelocity <= 0 ? 0 : yVelocity - yAcceleration;
			yPos -= yVelocity;
		}
	}
	
	public void checkHighestJump() {
		if (isJumping && yVelocity <= 0) {
			isJumping = false;
			isFalling = true;
			yVelocity = 0;
		}
	}
	
	public void checkStageBoundaryCollision() {
		if ((xPos + width) >= GameStage.WIDTH) {
			xPos = GameStage.WIDTH - width;
		}
		if (xPos <= 0) {
			xPos = 0;
		}
	}
	
	public void checkPlatformCollision(List<Platform> platforms) {
		if (dropDownTimer > 0) {
			dropDownTimer--;
			return;
		}
		
		boolean onAPlatformThisFrame = false;
		
			for (Platform platform : platforms) {
				boolean isCollidedXAxis = (xPos + width) > platform.getxPos() && xPos < platform.getxPos() + platform.getPaneWidth();
				boolean isLanding = isFalling && (yPos + height) <= platform.getyPos() && (yPos + height + yVelocity) >= platform.getyPos();
				boolean isStanding = Math.abs((yPos + height) - platform.getyPos()) < 1;				
				
				if (isCollidedXAxis && (isLanding || (isOnPlatform && isStanding))) {
					
					if (isLanding) {
						yPos = platform.getyPos() - height;
						yVelocity = 0;
						isFalling = false;
					}
					
					canJump = true;
					isOnPlatform = true;
					if (platform.getIsGround()) {
						canDropDown = false;
					} else {
						canDropDown = true;
					}
					onAPlatformThisFrame = true;
					
					break;
			}
		}
		
		if (!isFalling && !isJumping && !onAPlatformThisFrame) {
			isFalling = true;
			isOnPlatform = false;
			canJump = false;
		}
	}

	/**
	 * Handle player death when falling out of the playable area.
	 * Consider the player "dead" if their Y position moves beyond the bottom of the
	 * screen (plus their height), or far above the top bound (symmetry with enemies).
	 * Guarded to avoid repeated triggers while respawning/dying.
	 */
	public void handleOutOfBounds(GameStage gameStage) {
		if (isDying || respawnTimer > 0) {
			return;
		}
		if (yPos > GameStage.HEIGHT + height || yPos < -GameStage.HEIGHT) {
			die();
		}
	}
	
	public void isCollided(GameStage gameStage, int xOffset) {
		Boss boss = gameStage.getBoss();
		if (boss == null) {
			return;
		}

		int barrierX = Math.max(0, boss.getXPos() - xOffset);
		if (xPos + Player.width > barrierX) {
			xPos = Math.max(0, barrierX - Player.width);
			xVelocity = 0;
			setTranslateX(xPos);
		}

		Bounds playerBounds = this.localToParent(hitBox.getBoundsInParent());
		Bounds bossBounds = boss.getBoundsInParent();
		if (bossBounds.isEmpty()) {
			return;
		}

		if (!playerBounds.intersects(bossBounds)) {
			return;
		}

		// Safety fallback: if we somehow overlap with the boss, push the player back instead of killing.
		xPos = Math.max(0, barrierX - Player.width);
		setTranslateX(xPos);
		yVelocity = 0;
		isFalling = false;
	}
	
	public void enableKeys() {
		this.upKey = KeyCode.W;
		this.downKey = KeyCode.S;
		this.leftKey = KeyCode.A;
		this.rightKey = KeyCode.D;
		this.jumpKey = KeyCode.K;
		this.shootKey = KeyCode.L;
	}
	
	public void disableKeys() {
		this.upKey = null;
		this.downKey = null;
		this.leftKey = null;
		this.rightKey = null;
		this.jumpKey = null;
		this.shootKey = null;
	}
	
	
	// 				End of Movement Behaviors
	
	// GETTERS SETTERS
	public int getLives() {
		return this.lives;
	}

	public void setCurrentStage(GameStage stage) {
		this.currentStage = stage;
		refreshLivesLabel();
	}
	
	public KeyCode getLeftKey() {
		return this.leftKey;
	}

	public KeyCode getRightKey() {
		return rightKey;
	}

	public KeyCode getUpKey() {
		return upKey;
	}

	public KeyCode getDownKey() {
		return downKey;
	}
	
	public KeyCode getShootKey() {
		return shootKey;
	}

	public KeyCode getJumpKey() {
		return jumpKey;
	}

	public KeyCode getCheatKey(){
		return CheatKey;
	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}
	
	public boolean isProning() {
		return isProning;
	}

	public boolean isDying() { return isDying; }

	public SpriteAnimation getImageView() { return this.sprite;}
	
	public Rectangle getHitBox() { return this.hitBox; }
	
	public PlayerState getState() { return this.playerState; }
	public void setState(PlayerState playerState) {
		this.playerState = playerState;
	}

	public boolean isJumping() { return this.isJumping; }

	public boolean CheatOnandOff(KeyCode cheatKey) {
		return CheatActive = !CheatActive;}

	public void logPos() {
		logger.info("Player - X: {}, Y: {}", xPos, yPos);
	}

	public boolean isFalling() {
		return this.isFalling;
	}

	public void updateHorizontalMovement(boolean moving) {
		boolean previouslyMoving = this.isMovingHorizontally;
		this.wasMovingHorizontally = previouslyMoving;
		if (moving && !previouslyMoving && holdRunFrame) {
			pendingRunResume = true;
		}
		if (moving) {
			holdRunFrame = false;
		}
		this.isMovingHorizontally = moving;
	}

	public boolean hasJustStoppedMovingHorizontally() {
		return !isMovingHorizontally && wasMovingHorizontally;
	}

	public boolean consumePendingRunResume() {
		if (pendingRunResume) {
			pendingRunResume = false;
			return true;
		}
		return false;
	}

	public void latchRunFrameForIdle() {
		if (sprite.getCurrentDefinition() == ImageAssets.PLAYER_RUN) {
			lastRunFrameIndex = sprite.getCurrentFrameIndex();
		}
		if (hasWalkedOnce) {
			holdRunFrame = true;
		}
	}

	public boolean shouldHoldRunFrame() {
		return holdRunFrame;
	}

	public void clearRunFrameHold() {
		holdRunFrame = false;
		pendingRunResume = false;
	}

	public int getLastRunFrameIndex() {
		return lastRunFrameIndex;
	}

	public boolean hasRunHistory() {
		return hasWalkedOnce;
	}

	public void captureRunFrameIndex() {
		lastRunFrameIndex = sprite.getCurrentFrameIndex();
		hasWalkedOnce = true;
	}

	public void beginShootingHold(SpriteDefinition definition) {
		if (definition == null) {
			cancelShootingHold();
			return;
		}
		clearRunFrameHold();
		shootingHoldDefinition = definition;
		shootingHoldActive = true;
		shootingAnimationCompleted = false;
		shootingHoldUntilNanos = System.nanoTime() + SHOOT_HOLD_DURATION_NANOS;
	}

	public boolean isShootingHoldActive() {
		return shootingHoldActive;
	}

	public SpriteDefinition getShootingHoldDefinition() {
		return shootingHoldDefinition;
	}

	public boolean shouldAdvanceShootingAnimation() {
		return shootingHoldActive && !shootingAnimationCompleted;
	}

	public void markShootingAnimationCompleted() {
		shootingAnimationCompleted = true;
	}

	public boolean shouldReleaseShootingHold() {
		if (!shootingHoldActive) {
			return false;
		}
		return shootingAnimationCompleted || System.nanoTime() >= shootingHoldUntilNanos;
	}

	public void finishShootingHold() {
		shootingHoldActive = false;
		shootingAnimationCompleted = false;
		shootingHoldDefinition = null;
	}

	public void cancelShootingHold() {
		shootingHoldActive = false;
		shootingAnimationCompleted = false;
		shootingHoldDefinition = null;
	}

	// Laser mode controls
	public void toggleLaserMode() {
		isLaserMode = !isLaserMode;
		SoundController.getInstance().playRespawnSound();
	}

	public boolean isLaserMode() {
		return isLaserMode;
	}

	public KeyCode getSwitchBulletKey() {
		return switchBulletKey;
	}

	private void refreshLivesLabel() {
		if (currentStage == null) {
			return;
		}
		int remainingLives = lives;
		javafx.application.Platform.runLater(() -> currentStage.getLivesLabel().setText("Lives: " + remainingLives));
	}
	
}
