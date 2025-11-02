package se233.finalcontra.model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.controller.SpriteAnimation;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Enums.*;
import se233.finalcontra.view.GameStages.GameStage;

public class Enemy extends Pane {
    protected int xPos;
	protected int yPos;
	protected int width;
	protected int height;
    int health;
    private double speed;
    boolean alive = true;
    private EnemyType  type;
    private int shootTimer = 100;
    private SpriteAnimation sprite;
    private EnemyState enemyState;
    private int shootingAnimationTimer = 0;

    public Enemy(int xPos, int yPos, double speed, int width, int height, int spriteWidth, int spriteHeight, int count, int column, int row, Image image, int health, EnemyType type) {
    	setTranslateX(xPos);
    	setTranslateY(yPos);
        int effectiveColumns = Math.max(1, column);
        int effectiveRows = Math.max(1, row);

        int frameWidth = spriteWidth;
        if (image != null) {
            if (frameWidth <= 0 || frameWidth * effectiveColumns > image.getWidth()) {
                frameWidth = (int) Math.max(1, Math.floor(image.getWidth() / effectiveColumns));
            }
        } else if (frameWidth <= 0) {
            frameWidth = 1;
        }

        int frameHeight = spriteHeight;
        if (image != null) {
            if (frameHeight <= 0 || frameHeight * effectiveRows > image.getHeight()) {
                frameHeight = (int) Math.max(1, Math.floor(image.getHeight() / effectiveRows));
            }
        } else if (frameHeight <= 0) {
            frameHeight = 1;
        }

    	sprite = new SpriteAnimation(image, count, effectiveColumns, effectiveRows, 0, 0, frameWidth, frameHeight);
    	this.getChildren().add(sprite);
    	this.setWidth(width);
    	this.setHeight(height);
    	sprite.setFitHeight(height + 16);
    	sprite.setFitWidth(width + 16);
    	this.health = health;
        this.xPos = xPos;
        this.yPos = yPos;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public void updateWithPlayer(Player player, GameStage gameStage) {
    	if (health <= 0) kill();
        if (!alive || GameLoop.isPaused) return;

        if (type == EnemyType.FLYING) {
            flyTowardsPlayer(player);
            setState(EnemyState.ATTACKING);

        } else if (type == EnemyType.WALL_SHOOTER) {
            Bullet bullet = shootAtPlayer(player);
            if (bullet != null && !GameLoop.isPaused) {
                GameLoop.bullets.add(bullet);
                Platform.runLater(() -> {
                    gameStage.getChildren().add(bullet);
                });
            }
            setState(EnemyState.ATTACKING);
        }
    }

    private void flyTowardsPlayer(Player player) {
        double worldX = getTranslateX();
        double worldY = getTranslateY();

        // Account for parent offsets
        if (getParent() != null && getParent().getParent() != null) {
            worldX += getParent().getTranslateX();
            worldY += getParent().getTranslateY();
        }

        // Calculate centers
        Vector2D enemyCenter = new Vector2D(worldX + width / 2.0, worldY + height / 2.0);
        Vector2D playerCenter = new Vector2D(player.getxPos() + Player.width / 2.0,
                player.getyPos() + Player.height / 2.0);

        // Direction vector from enemy to player
        Vector2D direction = playerCenter.subtract(enemyCenter);
        double distance = direction.getLength();

        // Ignore invalid distances
        if (distance > 1000) return;
        if (distance < 0.1) return ;

        // Normalize and move
        direction = direction.normalize();

        if (Double.isNaN(direction.x) || Double.isNaN(direction.y)) {
            return;
        }

        double actualSpeed = (this.speed > 0 && this.speed < 100) ? this.speed : 3.0;

        // Apply movement in that direction
        double moveX = direction.x * actualSpeed;
        double moveY = direction.y * actualSpeed;

        if (Double.isNaN(moveX) || Double.isNaN(moveY)) return;

        // Update enemy position
        setTranslateX(getTranslateX() + moveX);
        setTranslateY(getTranslateY() + moveY);
    }



    public Bullet shootAtPlayer(Player player) {

        if (type == EnemyType.FLYING || GameLoop.isPaused) {
            return null;
        }

        if (shootTimer > 0) {
            shootTimer--;
            return null;
        }

        double worldX = getTranslateX();
        double worldY = getTranslateY();

        if (getParent() != null && getParent().getParent() != null) {
            worldX += getParent().getTranslateX();
            worldY += getParent().getTranslateY();
        }

        Vector2D enemyCenter = new Vector2D(worldX + width / 2.0, worldY + height / 2.0);
        Vector2D playerCenter = new Vector2D((double) (player.getxPos() + Player.width / 2.0), (double) (player.getyPos() + Player.height / 2.0));
        Vector2D directionToPlayer = playerCenter.subtract(enemyCenter);

        if (directionToPlayer.getLength() < 1 || directionToPlayer.getLength() > 1000) {
            System.out.println("Enemy too far from player or same position. Distance: " + directionToPlayer);
            return null;
        }

        if (directionToPlayer.getLength() == 0) {
            return null;
        }

        double bulletSpeed = 0.007;

        shootTimer = 100;
        shootingAnimationTimer = 20;
        SoundController.getInstance().playCannonSound();
        return new Bullet(
                enemyCenter,
                directionToPlayer,
                (double) bulletSpeed,
                BulletOwner.ENEMY
        );
    }


    private ShootingDirection determineDirection(Vector2D direction) {
        // Calculate angle in degrees
        double angle = Math.toDegrees(Math.atan2(direction.y, direction.x));
        double degrees = Math.toDegrees(angle);
        if (degrees < 0) degrees += 360;

        // Map to 8 directions
        if (degrees >= 337.5 || degrees < 22.5) {
            return ShootingDirection.RIGHT;
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return ShootingDirection.DOWN_RIGHT;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return ShootingDirection.DOWN_LEFT;
        } else if (degrees >= 112.5 && degrees < 202.5) {
            return ShootingDirection.LEFT;
        } else if (degrees >= 202.5 && degrees < 292.5) {
            return ShootingDirection.UP_LEFT;
        } else {
            return ShootingDirection.UP_RIGHT;
        }
    }
    
    // Reduce Enemy HP, and Add Game score
    public void takeDamage(int damage, Boss boss) {
    	health -= damage;
    		switch (type) {
        	case MINION: break;
        	case WALL_SHOOTER: break;
        	case TURRET: SoundController.getInstance().playBlockHitSound(); break;
        	case FLYING: break;
        	case WALL: SoundController.getInstance().playBlockHitSound(); break;
    	}
    	
    	if (health <= 0) {
    		switch (type) {
    		case MINION: GameLoop.addScore(300); SoundController.getInstance().playDieSound(); GameStage.totalMinions--; break;
    		case WALL_SHOOTER: GameLoop.addScore(100); SoundController.getInstance().playDieSound(); break;
    		case FLYING: GameLoop.addScore(150); SoundController.getInstance().playDieSound(); break;
    		case WALL: GameLoop.addScore(1000); SoundController.getInstance().playExplosionSound(); boss.getWeakPoints().clear(); break;
            case JAVAHEAD: GameLoop.addScore(2500); SoundController.getInstance().playJavaDieSound(); boss.getWeakPoints().clear(); break;
			default: 
				break;
    		}
    		kill();
            setState(EnemyState.DEAD);
    	}
    }

    public int getShootingAnimationTimer() {
        return shootingAnimationTimer;
    }

    public void updateShootingAnimation() {
        if (shootingAnimationTimer > 0) {
            shootingAnimationTimer--;
        }
    }
    
    public void setShootingAnimationTimer(int timer) {
        this.shootingAnimationTimer = timer;
    }


    public double getXPos() { return xPos; }
    public double getYPos() { return yPos; }
    public double getW() { return width; }
    public double getH() { return height; }
    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }
    public EnemyState  getState() { return enemyState;}
    public void setState(EnemyState enemyState) { this.enemyState = enemyState;}
    public EnemyType getType() { return type; }
    public SpriteAnimation getSprite() { return sprite; }

}
