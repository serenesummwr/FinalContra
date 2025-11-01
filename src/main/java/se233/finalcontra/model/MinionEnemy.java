package se233.finalcontra.model;

import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.view.Platform;
import se233.finalcontra.view.GameStages.GameStage;

public class MinionEnemy extends Enemy {

	private int startX;
	private int startY;
	private double xVelocity = 0;
	private double xAcceleration = 1;
	private double yVelocity = 0;
	private double yAcceleration = 0.40d;
	private double xMaxVelocity = 2;
	private double yMaxVelocity = 10;
    boolean isAlive = true;
    boolean isMoveLeft = false;
    boolean isMoveRight = false;
    boolean isFalling = true;
    boolean canJump = false;
    boolean isJumping = false;
	private boolean isOnPlatform;
	Rectangle hitBox;
	private static final int SAFE_DROP_DISTANCE = 280;
	
	public MinionEnemy(int xPos, int yPos, double speed, int width, int height, int count, int column, int row, Image image, int health, EnemyType type) {
		super(xPos, yPos, speed, width, height, 64, 64, count, column, row, image, health, type);
		hitBox = new Rectangle(width, height);
		hitBox.setFill(Color.TRANSPARENT);
		this.getChildren().add(hitBox);
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

	public void checkStageBoundaryCollision() {
		if ((xPos + width) >= GameStage.WIDTH) {
			xPos = GameStage.WIDTH - width;
		}
		if (xPos <= 0) {
			xPos = 0;
		}
	}
	
		public void checkPlatformCollision(List<Platform> platforms) {		
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
	public void repaint() {
		moveY();
		moveX();
	}
	
    public void stop() {
        isMoveLeft = false;
        isMoveRight = false;
    }

	public void jump() {
		if (canJump) {
			yVelocity = yMaxVelocity;
			canJump = false;
			isJumping = true;
			isFalling = false;
			isOnPlatform = false;
		}
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
	
    public void updateAI(Player player, GameStage gameStage) {
		if (!isAlive()) {
			return;
		}

		List<Platform> platforms = gameStage.getPlatforms();
		double enemyCenterX = xPos + (double) width / 2;
		double playerCenterX = player.getxPos() + (double) Player.width / 2;
		double horizontalDiff = playerCenterX - enemyCenterX;
		double absHorizontalDiff = Math.abs(horizontalDiff);

		double enemyFeetY = yPos + height;
		double playerFeetY = player.getyPos() + Player.height;
		boolean playerAbove = playerFeetY < enemyFeetY - 12;
		boolean playerBelow = playerFeetY > enemyFeetY + 36;

		boolean pursueHorizontally = absHorizontalDiff > 12 || (!playerAbove && absHorizontalDiff > 8);

		if (pursueHorizontally) {
			boolean movingRight = horizontalDiff > 0;
			boolean movingLeft = horizontalDiff < 0;
			boolean hasLandingAhead = true;
			if (isOnPlatform) {
				if (movingRight) {
					hasLandingAhead = hasSafeLandingAhead(true, platforms);
				} else if (movingLeft) {
					hasLandingAhead = hasSafeLandingAhead(false, platforms);
				}
			}
			boolean shouldAvoidEdge = isOnPlatform && !hasLandingAhead;

			if (!shouldAvoidEdge) {
				if (movingRight) {
					getSprite().tick();
					moveRight();
				} else if (movingLeft) {
					getSprite().tick();
					moveLeft();
				}
			} else {
				stop();
			}
		} else if (!playerAbove) {
			stop();
		}

		if (playerAbove && canJump && absHorizontalDiff < 260) {
			if (horizontalDiff > 14) {
				moveRight();
			} else if (horizontalDiff < -14) {
				moveLeft();
			}
			jump();
		}

		if (playerAbove && !isOnPlatform) {
			if (horizontalDiff > 6) {
				moveRight();
			} else if (horizontalDiff < -6) {
				moveLeft();
			}
		}
    }

	private boolean hasSafeLandingAhead(boolean movingRight, List<Platform> platforms) {
		int checkX = movingRight ? xPos + width + 6 : xPos - 6;
		int footY = yPos + height;

		if (checkX < 0 || checkX > GameStage.WIDTH) {
			return false;
		}

		int maxDropY = footY + SAFE_DROP_DISTANCE;

		for (Platform platform : platforms) {
			int platformLeft = platform.getxPos();
			int platformRight = platformLeft + platform.getPaneWidth();
			if (checkX < platformLeft || checkX > platformRight) {
				continue;
			}

			int platformTop = platform.getyPos();
			if (platformTop < footY - 12) {
				continue;
			}

			if (platformTop <= maxDropY) {
				return true;
			}
		}

		return false;
	}

	public void handleOutOfBounds(GameStage gameStage) {
		if (!isAlive()) {
			return;
		}
		if (yPos > GameStage.HEIGHT + height || yPos < -GameStage.HEIGHT) {
			takeDamage(Math.max(1, health), gameStage.getBoss());
		}
	}

    // Check
    public void checkReachGameWall() {
        if(xPos <= 0) {
            xPos = 0;
        } else if (xPos+getWidth() >= GameStage.WIDTH) {
        	xPos = GameStage.WIDTH-(int)getWidth();
        }
    }
    public void checkReachHighest () {
        if(isJumping && yVelocity <= 0) {
            isJumping = false;
            isFalling = true;
            yVelocity = 0;
        }
    }
    
    
    public Rectangle getHitBox() { return this.hitBox; }

}
