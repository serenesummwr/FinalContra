package se233.finalcontra.model;

import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.view.GameStages.GameStage;
import se233.finalcontra.view.Platform;

public class MinionEnemy extends Enemy {

	private static final double DEFAULT_X_ACCELERATION = 0.5;
	private static final double DEFAULT_Y_ACCELERATION = 0.40d;
	private static final double DEFAULT_X_MAX_VELOCITY = 2.35;
	private static final double DEFAULT_Y_MAX_VELOCITY = 9.5;
	private static final double STOP_THRESHOLD = 6.0;
	private static final double DETECTION_RANGE = GameStage.WIDTH;
	private static final double RETURN_THRESHOLD = 12.0;
	private static final int SAFE_DROP_DISTANCE = 320;
	private static final int DROP_COOLDOWN_FRAMES = 45;
	private static final int DROP_IGNORE_FRAMES = 12;
	private static final double AIR_STEER_THRESHOLD = 4.0;
	private static final int MAX_JUMP_VERTICAL = 160;
	private static final int MAX_JUMP_HORIZONTAL = 260;
	private static final double JUMP_ALIGN_THRESHOLD = 18.0;
	private static final double SEPARATION_PUSH = 2.4;

	private final int spawnX;
	private final int spawnY;

	private double xVelocity = 0;
	private double yVelocity = 0;
	private double xAcceleration = DEFAULT_X_ACCELERATION;
	private double yAcceleration = DEFAULT_Y_ACCELERATION;
	private double xMaxVelocity = DEFAULT_X_MAX_VELOCITY;
	private double yMaxVelocity = DEFAULT_Y_MAX_VELOCITY;

	private boolean isMoveLeft = false;
	private boolean isMoveRight = false;
	private boolean isJumping = false;
	private boolean isFalling = true;
	private boolean canJump = false;
	private boolean isOnPlatform = false;

	private int dropIgnoreTimer = 0;
	private int dropCooldownTimer = 0;

	private final Rectangle hitBox;

	private static final class JumpTarget {
		final Platform platform;
		final double landingX;
		final int landingY;
		final double takeoffX;
		final double horizontalDistance;
		final double verticalRise;
		final double score;
		final boolean towardRight;

		JumpTarget(Platform platform, double landingX, int landingY, double takeoffX,
				double horizontalDistance, double verticalRise, double score, boolean towardRight) {
			this.platform = platform;
			this.landingX = landingX;
			this.landingY = landingY;
			this.takeoffX = takeoffX;
			this.horizontalDistance = horizontalDistance;
			this.verticalRise = verticalRise;
			this.score = score;
			this.towardRight = towardRight;
		}
	}

	public MinionEnemy(int xPos, int yPos, double speed, int width, int height, int count, int column, int row,
			Image image, int health, EnemyType type) {
		super(xPos, yPos, speed, width, height, 64, 64, count, column, row, image, health, type);
		this.spawnX = xPos;
		this.spawnY = yPos;
		this.hitBox = new Rectangle(width, height);
		this.hitBox.setFill(Color.TRANSPARENT);
		this.getChildren().add(hitBox);
		updateHitBoxLocation();
		setTranslateX(xPos);
		setTranslateY(yPos);
	}

	public void updateAI(Player player, GameStage gameStage) {
		if (!isAlive()) {
			return;
		}

		if (dropCooldownTimer > 0) {
			dropCooldownTimer--;
		}

		List<Platform> platforms = gameStage.getPlatforms();
		if (platforms == null || platforms.isEmpty()) {
			stop();
			return;
		}

		double enemyCenterX = getCenterX();
		double playerCenterX = player.getxPos() + (double) Player.width / 2;
		double horizontalDiff = playerCenterX - enemyCenterX;
		double absHorizontalDiff = Math.abs(horizontalDiff);

		int enemyFootY = yPos + height;
		double playerFeetY = player.getyPos() + Player.height;
		boolean playerAbove = playerFeetY < enemyFootY - Player.height * 0.25;
		boolean playerBelow = playerFeetY > enemyFootY + Player.height * 0.25;

		boolean safeLandingAheadRight = hasSafeLandingAhead(true, platforms);
		boolean safeLandingAheadLeft = hasSafeLandingAhead(false, platforms);
		boolean safeDropAheadRight = hasSafeDropBelow(true, platforms);
		boolean safeDropAheadLeft = hasSafeDropBelow(false, platforms);
		JumpTarget jumpTargetRight = findJumpTarget(true, platforms, playerCenterX);
		JumpTarget jumpTargetLeft = findJumpTarget(false, platforms, playerCenterX);

		boolean playerInRange = absHorizontalDiff <= DETECTION_RANGE;
		boolean isMoving = false;

		boolean desireRight = horizontalDiff > STOP_THRESHOLD;
		boolean desireLeft = horizontalDiff < -STOP_THRESHOLD;

		if (playerInRange && playerBelow && isOnPlatform && dropIgnoreTimer == 0 && dropCooldownTimer == 0) {
			boolean dropRight = horizontalDiff >= 0;
			boolean safeDrop = dropRight ? safeDropAheadRight : safeDropAheadLeft;
			if (safeDrop) {
				beginDropDown();
				dropCooldownTimer = DROP_COOLDOWN_FRAMES;
			}
		}

		JumpTarget selectedJump = null;
		Boolean jumpTowardRight = null;

		if (playerInRange) {
			boolean prefersRight = horizontalDiff >= 0;

			if (playerAbove && !isJumping && !isFalling) {
				if (prefersRight && jumpTargetRight != null) {
					selectedJump = jumpTargetRight;
					jumpTowardRight = Boolean.TRUE;
				} else if (!prefersRight && jumpTargetLeft != null) {
					selectedJump = jumpTargetLeft;
					jumpTowardRight = Boolean.FALSE;
				}
			}

			boolean pathBlockedRight = isOnPlatform && !playerBelow && desireRight && !safeLandingAheadRight;
			boolean pathBlockedLeft = isOnPlatform && !playerBelow && desireLeft && !safeLandingAheadLeft;

			if (selectedJump == null) {
				if (pathBlockedRight && jumpTargetRight != null) {
					selectedJump = jumpTargetRight;
					jumpTowardRight = Boolean.TRUE;
				} else if (pathBlockedLeft && jumpTargetLeft != null) {
					selectedJump = jumpTargetLeft;
					jumpTowardRight = Boolean.FALSE;
				}
			}

			boolean jumpEngaged = false;
			if (selectedJump != null && jumpTowardRight != null) {
				jumpEngaged = prepareOrExecuteJump(selectedJump, jumpTowardRight.booleanValue());
				if (jumpEngaged) {
					isMoving = true;
					if (jumpTowardRight.booleanValue()) {
						desireRight = true;
						desireLeft = false;
					} else {
						desireRight = false;
						desireLeft = true;
					}
				}
			}

			if (!jumpEngaged) {
				if (desireRight) {
					if (!safeLandingAheadRight && jumpTargetRight == null) {
						stop();
					} else {
						moveRight();
						isMoving = true;
					}
				} else if (desireLeft) {
					if (!safeLandingAheadLeft && jumpTargetLeft == null) {
						stop();
					} else {
						moveLeft();
						isMoving = true;
					}
				} else {
					stop();
				}
			}
		} else {
			double horizontalHomeDiff = spawnX - xPos;
			if (Math.abs(horizontalHomeDiff) > RETURN_THRESHOLD) {
				if (horizontalHomeDiff > 0) {
					if (!isOnPlatform || safeLandingAheadRight) {
						moveRight();
						isMoving = true;
					}
				} else {
					if (!isOnPlatform || safeLandingAheadLeft) {
						moveLeft();
						isMoving = true;
					}
				}
			} else {
				stop();
			}
		}

		if (isJumping || isFalling) {
			if (horizontalDiff > AIR_STEER_THRESHOLD) {
				if (safeLandingAheadRight || (jumpTargetRight != null && jumpTargetRight.towardRight)) {
					moveRight();
					isMoving = true;
				}
			} else if (horizontalDiff < -AIR_STEER_THRESHOLD) {
				if (safeLandingAheadLeft || (jumpTargetLeft != null && !jumpTargetLeft.towardRight)) {
					moveLeft();
					isMoving = true;
				}
			}
		}

		if (isMoving || isJumping || isFalling) {
			getSprite().tick();
		}
	}

	public void repaint() {
		moveY();
		moveX();
		applySeparation();
		updateHitBoxLocation();
	}

	public void moveLeft() {
		isMoveRight = false;
		isMoveLeft = true;
		this.setScaleX(1);
	}

	public void moveRight() {
		isMoveRight = true;
		isMoveLeft = false;
		this.setScaleX(-1);
	}

	public void stop() {
		isMoveLeft = false;
		isMoveRight = false;
		xVelocity = 0;
	}

	public void jump() {
		if (canJump) {
			yVelocity = yMaxVelocity;
			canJump = false;
			isJumping = true;
			isFalling = false;
			isOnPlatform = false;
			getSprite().tick();
		}
	}

	private void moveX() {
		double maxVelocity = xMaxVelocity;
		if (isMoveRight) {
			xVelocity = Math.min(maxVelocity, xVelocity + xAcceleration);
			xPos += xVelocity;
		} else if (isMoveLeft) {
			xVelocity = Math.min(maxVelocity, xVelocity + xAcceleration);
			xPos -= xVelocity;
		} else {
			xVelocity = Math.max(0, xVelocity - xAcceleration * 0.6);
			if (xVelocity < 0.1) {
				xVelocity = 0;
			}
		}
		setTranslateX(xPos);
	}

	private void moveY() {
		if (isFalling) {
			yVelocity = Math.min(yMaxVelocity, yVelocity + yAcceleration);
			yPos += yVelocity;
		} else if (isJumping) {
			yVelocity = Math.max(0, yVelocity - yAcceleration);
			yPos -= yVelocity;
		}
		setTranslateY(yPos);
	}

	public void checkReachGameWall() {
		if (xPos <= 0) {
			xPos = 0;
		} else if (xPos + width >= GameStage.WIDTH) {
			xPos = GameStage.WIDTH - width;
		}
		setTranslateX(xPos);
	}

	public void checkReachHighest() {
		if (isJumping && yVelocity <= 0) {
			isJumping = false;
			isFalling = true;
			yVelocity = 0;
		}
	}

	public void checkPlatformCollision(List<Platform> platforms) {
		if (dropIgnoreTimer > 0) {
			dropIgnoreTimer--;
			isOnPlatform = false;
			canJump = false;
			return;
		}

		boolean onAPlatformThisFrame = false;

		for (Platform platform : platforms) {
			boolean isCollidedXAxis = (xPos + width) > platform.getxPos()
					&& xPos < platform.getxPos() + platform.getPaneWidth();
			boolean isLanding = isFalling && (yPos + height) <= platform.getyPos()
					&& (yPos + height + yVelocity) >= platform.getyPos();
			boolean isStanding = Math.abs((yPos + height) - platform.getyPos()) < 1;

			if (isCollidedXAxis && (isLanding || (isOnPlatform && isStanding))) {
				if (isLanding) {
					yPos = platform.getyPos() - height;
					yVelocity = 0;
					isFalling = false;
					isJumping = false;
				}

				canJump = true;
				isOnPlatform = true;
				onAPlatformThisFrame = true;
				break;
			}
		}

		if (!onAPlatformThisFrame) {
			if (!isJumping) {
				isFalling = true;
				canJump = false;
			}
			isOnPlatform = false;
		}
	}

	private double getCenterX() {
		return xPos + (double) width / 2;
	}

	private double getCenterY() {
		return yPos + (double) height / 2;
	}

	private JumpTarget findJumpTarget(boolean towardRight, List<Platform> platforms, double playerCenterX) {
		if (platforms == null || platforms.isEmpty()) {
			return null;
		}

		int footY = yPos + height;
		double enemyCenterX = getCenterX();
		double centerOffset = width / 2.0;
		double alignOffset = Math.max(12.0, width * 0.35);
		double originX = towardRight ? xPos + width : xPos;

		JumpTarget best = null;

		for (Platform platform : platforms) {
			int platformTop = platform.getyPos();
			if (platformTop >= footY - 6) {
				continue;
			}

			int verticalRise = footY - platformTop;
			if (verticalRise > MAX_JUMP_VERTICAL) {
				continue;
			}

			int left = platform.getxPos();
			int right = left + platform.getPaneWidth();

			if (towardRight) {
				if (right <= originX + 4) {
					continue;
				}
			} else {
				if (left >= originX - 4) {
					continue;
				}
			}

			double landingMin = left + centerOffset;
			double landingMax = right - centerOffset;
			if (landingMax <= landingMin) {
				double mid = (left + right) / 2.0;
				landingMin = mid;
				landingMax = mid;
			}

			double landingX = Math.max(landingMin, Math.min(landingMax, playerCenterX));
			double horizontalDistance = Math.abs(landingX - enemyCenterX);
			if (horizontalDistance > MAX_JUMP_HORIZONTAL) {
				continue;
			}

			double takeoffX = towardRight
					? Math.min(originX + MAX_JUMP_HORIZONTAL, left - alignOffset)
					: Math.max(originX - MAX_JUMP_HORIZONTAL, right + alignOffset);

			if (takeoffX < centerOffset || takeoffX > GameStage.WIDTH - centerOffset) {
				continue;
			}

			int supportY = findNextLandingY((int) Math.round(takeoffX), platforms, footY - 6);
			if (supportY == -1 || Math.abs(supportY - footY) > 2) {
				continue;
			}

			double playerDistanceAfter = Math.abs(playerCenterX - landingX);
			double playerDistanceBefore = Math.abs(playerCenterX - enemyCenterX);
			double score = verticalRise * 1.7 + horizontalDistance;
			if (playerDistanceAfter >= playerDistanceBefore) {
				score += 120;
			} else {
				score -= (playerDistanceBefore - playerDistanceAfter) * 0.8;
			}
			score += Math.abs(landingX - enemyCenterX) * 0.3;

			if (best == null || score < best.score) {
				best = new JumpTarget(platform, landingX, platformTop, takeoffX, horizontalDistance, verticalRise,
						score, towardRight);
			}
		}

		return best;
	}

	private boolean prepareOrExecuteJump(JumpTarget target, boolean towardRight) {
		if (target == null) {
			return false;
		}

		double centerX = getCenterX();
		double delta = target.takeoffX - centerX;
		double alignRange = Math.max(JUMP_ALIGN_THRESHOLD, width * 0.3);

		if (!isJumping && !isFalling) {
			if (Math.abs(delta) > alignRange) {
				if (delta > 0) {
					moveRight();
				} else {
					moveLeft();
				}
				return true;
			}

			if (!canJump) {
				return false;
			}

			if (towardRight) {
				moveRight();
			} else {
				moveLeft();
			}
			jump();
			return true;
		}

		if (towardRight) {
			moveRight();
		} else {
			moveLeft();
		}
		return true;
	}

	private boolean hasSafeLandingAhead(boolean movingRight, List<Platform> platforms) {
		int footY = yPos + height;
		int maxDropY = footY + SAFE_DROP_DISTANCE;

		int[] sampleOffsets = new int[] {
				Math.max(8, width / 3),
				Math.max(12, (int) (width * 0.75)),
				width + 24
		};

		for (int offset : sampleOffsets) {
			int checkX = movingRight ? xPos + offset : xPos - offset;
			if (checkX < 0 || checkX > GameStage.WIDTH) {
				continue;
			}

			int landingY = findNextLandingY(checkX, platforms, footY - 4);
			if (landingY == -1 || landingY > maxDropY) {
				continue;
			}

			return true;
		}

		return false;
	}

	private boolean hasSafeDropBelow(boolean movingRight, List<Platform> platforms) {
		int footY = yPos + height;
		int minDropY = footY + Math.max(12, height / 4);
		int maxDropY = footY + SAFE_DROP_DISTANCE;

		int[] sampleOffsets = new int[] {
				Math.max(8, width / 3),
				Math.max(12, (int) (width * 0.75)),
				width + 24
		};

		for (int offset : sampleOffsets) {
			int checkX = movingRight ? xPos + offset : xPos - offset;
			if (checkX < 0 || checkX > GameStage.WIDTH) {
				continue;
			}

			int landingY = findNextLandingY(checkX, platforms, minDropY);
			if (landingY == -1 || landingY > maxDropY) {
				continue;
			}

			return true;
		}

		return false;
	}

	private int findNextLandingY(int x, List<Platform> platforms, int minimumY) {
		int bestLanding = -1;
		for (Platform platform : platforms) {
			int left = platform.getxPos();
			int right = left + platform.getPaneWidth();
			if (x < left || x > right) {
				continue;
			}
			int top = platform.getyPos();
			if (top < minimumY) {
				continue;
			}
			if (bestLanding == -1 || top < bestLanding) {
				bestLanding = top;
			}
		}
		return bestLanding;
	}

	private void applySeparation() {
		if (!isOnPlatform || isJumping || isFalling) {
			return;
		}

		List<Enemy> enemies = GameLoop.enemies;
		if (enemies == null || enemies.isEmpty()) {
			return;
		}

		double centerX = getCenterX();
		double centerY = getCenterY();
		double halfWidth = width / 2.0;
		double halfHeight = height / 2.0;
		boolean adjusted = false;

		for (Enemy enemy : enemies) {
			if (enemy == this || !(enemy instanceof MinionEnemy other)) {
				continue;
			}
			if (!other.isAlive()) {
				continue;
			}

			double otherCenterX = other.getXPos() + other.getW() / 2.0;
			double otherCenterY = other.getYPos() + other.getH() / 2.0;
			double dx = centerX - otherCenterX;
			double dy = centerY - otherCenterY;

			double verticalThreshold = halfHeight + other.getH() / 2.0;
			if (Math.abs(dy) > verticalThreshold) {
				continue;
			}

			double combinedHalfWidth = halfWidth + other.getW() / 2.0;
			double overlap = combinedHalfWidth - Math.abs(dx);
			if (overlap > 0) {
				double push = Math.max(SEPARATION_PUSH, overlap * 0.35);
				if (dx >= 0) {
					xPos += push;
				} else {
					xPos -= push;
				}
				centerX = getCenterX();
				adjusted = true;
			}
		}

		if (adjusted) {
			if (xPos < 0) {
				xPos = 0;
			} else if (xPos + width > GameStage.WIDTH) {
				xPos = GameStage.WIDTH - width;
			}
			setTranslateX(xPos);
		}
	}

	private void beginDropDown() {
		isOnPlatform = false;
		canJump = false;
		isFalling = true;
		dropIgnoreTimer = DROP_IGNORE_FRAMES;
		yVelocity = Math.min(1.5, Math.max(yVelocity, 1.0));
		yPos += 1;
		setTranslateY(yPos);
	}

	public void handleOutOfBounds(GameStage gameStage) {
		if (!isAlive()) {
			return;
		}
		if (yPos > GameStage.HEIGHT + height || yPos < -GameStage.HEIGHT) {
			takeDamage(Math.max(1, health), gameStage.getBoss());
		}
	}

	private void updateHitBoxLocation() {
		hitBox.setWidth(width);
		hitBox.setHeight(height);
		hitBox.setTranslateX(0);
		hitBox.setTranslateY(0);
	}

	public Rectangle getHitBox() {
		return this.hitBox;
	}
}
