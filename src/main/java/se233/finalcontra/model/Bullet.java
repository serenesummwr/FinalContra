package se233.finalcontra.model;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import se233.finalcontra.controller.SpriteAnimation;
import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.BulletType;
import se233.finalcontra.model.Enums.ShootingDirection;

public class Bullet extends Pane {
    private Vector2D velocity;
	private Vector2D position;
    private ShootingDirection direction;
	private BulletOwner owner;
	private boolean Alive = true;
	private SpriteAnimation sprite;
	private boolean gravityEnabled = false;
	private double velocityX;
	private double velocityY;
	private static final double GRAVITY = 0.3;
	private BulletType type;
	private int damage;
	private int remainingHits;

    public Bullet(int xPos, int yPos, int speedX, int speedY, ShootingDirection direction , BulletOwner owner) {
		this(xPos, yPos, speedX, speedY, direction, owner, BulletType.NORMAL);
    }

    public Bullet(int xPos, int yPos, int speedX, int speedY, ShootingDirection direction , BulletOwner owner, BulletType type) {
    	this.direction = direction;
        this.position = new Vector2D(xPos, yPos);
		this.velocity =  calculateVelocity(speedX, speedY, direction);
		this.owner = owner;
		this.type = type;

		this.velocityX = speedX;
		this.velocityY = speedY;
		applyTypeDefaults();
		setupBullet();
    }

	public void setGravityEnabled(boolean enabled) {
		this.gravityEnabled = enabled;
	}

	public Bullet(Vector2D startPos, Vector2D directionVector, double speed, BulletOwner owner) {
		this(startPos, directionVector, speed, owner, BulletType.NORMAL);
	}

	public Bullet(Vector2D startPos, Vector2D directionVector, double speed, BulletOwner owner, BulletType type) {
		this.position = new Vector2D(startPos.x, startPos.y);
		this.owner = owner;
		this.type = type;
		this.direction = directionVector.x < 0 ? ShootingDirection.LEFT : ShootingDirection.RIGHT;
		Vector2D normalized = directionVector.normalize();
		if (normalized.getLength() == 0) {
			normalized = new Vector2D(this.direction == ShootingDirection.LEFT ? -1 : 1, 0);
		}
		this.velocity = normalized.multiply(speed);
		this.velocityX = velocity.x;
		this.velocityY = velocity.y;

		applyTypeDefaults();
		setupBullet();
	}

	private void setupBullet() {
		setTranslateX(position.x);
		setTranslateY(position.y);
		
		if (this.owner == BulletOwner.PLAYER) {
			if (this.type == BulletType.LASER) {
				int frameWidth = (int) ImageAssets.LASER_IMAGE.getWidth();
				int frameHeight = (int) ImageAssets.LASER_IMAGE.getHeight();
				sprite = new SpriteAnimation(ImageAssets.LASER_IMAGE, 1, 1, 1, 0, 0, frameWidth, frameHeight);
				sprite.setFitWidth(160);
				sprite.setFitHeight(40);
				sprite.setPreserveRatio(true);
				sprite.setOpacity(0.9);
				applyLaserEffects();
			} else {
				int frameWidth = 9;
				int frameHeight = 18;
				sprite = new SpriteAnimation(ImageAssets.BULLET_IMAGE, 6, 6, 1, 0, 0, frameWidth, frameHeight);
				sprite.setFitWidth(36);
				sprite.setFitHeight(36);
			}
		} else {
			if (this.type == BulletType.BOSS3) {
				int frameWidth = (int) Math.max(1, ImageAssets.BOSS3_BULLET_IMAGE.getWidth());
				int frameHeight = (int) Math.max(1, ImageAssets.BOSS3_BULLET_IMAGE.getHeight());
				sprite = new SpriteAnimation(ImageAssets.BOSS3_BULLET_IMAGE, 1, 1, 1, 0, 0, frameWidth, frameHeight);
				sprite.setPreserveRatio(true);
				sprite.setFitWidth(56);
				sprite.setFitHeight(56);
				applyBoss3Effects();
			} else if (this.type == BulletType.JAVA_ORB) {
				int frameWidth = (int) Math.max(1, ImageAssets.JAVA_SKILL.getWidth());
				int frameHeight = (int) Math.max(1, ImageAssets.JAVA_SKILL.getHeight());
				sprite = new SpriteAnimation(ImageAssets.JAVA_SKILL, 1, 1, 1, 0, 0, frameWidth, frameHeight);
				sprite.setPreserveRatio(true);
				sprite.setFitWidth(72);
				sprite.setFitHeight(72);
				applyJavaOrbEffects();
			} else {
				sprite = new SpriteAnimation(ImageAssets.CANNONBALL_IMAGE, 1, 1, 1, 0, 0, 32, 32);
				sprite.setFitHeight(32);
				sprite.setFitWidth(32);
			}
		}
		
		this.getChildren().add(sprite);
		this.setWidth(sprite.getFitWidth());
		this.setHeight(sprite.getFitHeight());
		alignSpriteToVelocity();
	}
	
	// Method สำหรับอัปเดตแอนิเมชันของกระสุน
	public void tick() {
		if (sprite != null) {
			sprite.tick();
		}
	}

	private void applyTypeDefaults() {
		this.damage = 500;
		this.remainingHits = 1;
		if (this.type == null) {
			this.type = BulletType.NORMAL;
		}

		if (this.owner == BulletOwner.PLAYER && this.type == BulletType.LASER) {
			this.damage = 1500;
			this.remainingHits = 3;
			Vector2D currentVelocity = this.velocity != null ? this.velocity : new Vector2D(velocityX, velocityY);
			if (currentVelocity.getLength() > 0) {
				this.velocity = currentVelocity.normalize().multiply(18);
				this.velocityX = this.velocity.x;
				this.velocityY = this.velocity.y;
			}
		} else if (this.type == BulletType.BOSS3) {
			this.damage = 800;
			this.remainingHits = 1;
		} else if (this.owner == BulletOwner.ENEMY && this.type == BulletType.JAVA_ORB) {
			this.damage = 900;
			this.remainingHits = 1;
		}
	}

	private void applyLaserEffects() {
		DropShadow aura = new DropShadow(25, Color.AQUA);
		aura.setSpread(0.45);
		Glow glow = new Glow(0.85);
		glow.setInput(aura);
		sprite.setEffect(glow);
	}

	private void applyBoss3Effects() {
		DropShadow aura = new DropShadow(30, Color.CRIMSON);
		aura.setSpread(0.65);
		aura.setRadius(35);
		Glow glow = new Glow(0.75);
		glow.setInput(aura);
		sprite.setEffect(glow);
	}

	private void applyJavaOrbEffects() {
		DropShadow aura = new DropShadow(28, Color.DODGERBLUE);
		aura.setSpread(0.55);
		aura.setRadius(30);
		Glow glow = new Glow(0.82);
		glow.setInput(aura);
		sprite.setEffect(glow);
	}

	private void alignSpriteToVelocity() {
		Vector2D vel = this.velocity != null ? this.velocity : new Vector2D(velocityX, velocityY);
		if (vel.getLength() == 0 || sprite == null) {
			return;
		}
		double angle = Math.toDegrees(Math.atan2(vel.y, vel.x));
		sprite.setRotate(angle);
	}

	private Vector2D calculateVelocity(int speedX, int speedY, ShootingDirection direction) {
		switch (direction) {
			case LEFT: this.setRotate(180);
				return new Vector2D(-speedX, 0);
			case RIGHT:
				return new Vector2D(speedX, 0);
			case UP: this.setRotate(270);
				return new Vector2D(0, -speedY);
			case UP_LEFT: this.setRotate(225);
				return new Vector2D(-speedX - 2, -speedY + 2);
			case UP_RIGHT: this.setRotate(-45);
				return new Vector2D(speedX + 2, -speedY - 2);
			case DOWN_LEFT: this.setRotate(135);
				return new Vector2D(-speedX - 2, speedY - 2);
			case DOWN_RIGHT: this.setRotate(-315);
				return new Vector2D(speedX + 2, speedY - 2);
			default:
				return new Vector2D(0, 0);
		}
	}

	public void move() {
		if (gravityEnabled) {
			velocityY += GRAVITY; // Apply gravity
			setTranslateX(getTranslateX() + velocityX);
			setTranslateY(getTranslateY() + velocityY);
		} else {
			position = position.add(velocity);
			setTranslateX(position.x);
			setTranslateY(position.y);
		}
	}

	public void move(float deltaTime) {
		Vector2D movement = velocity.multiply(deltaTime);
		position = position.add(movement);
		setTranslateX(position.x);
		setTranslateY(position.y);
	}


	public Vector2D getPosition() {
		return position;
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D newVelocity) {
		this.velocity = newVelocity;
	}

	public int getxPos() {
		return (int)position.x;
	}

	public int getyPos() {
		return (int)position.y;
	}

	public boolean isAlive() { return Alive; }
	public void destroy() { Alive = false; }

	public boolean isOutOfBounds(int screenWidth, int screenHeight) {
		return position.x < -5 || position.x > screenWidth + 5 ||
				position.y < -5 || position.y > screenHeight + 5;
	}

	public BulletOwner getOwner() { return owner; }
	public boolean isEnemyBullet() { return owner == BulletOwner.ENEMY;}
	
	public void setBulletSprite(Image image, int frameCount, int columns, int rows) {
		// เปลี่ยน sprite animation ใหม่
		sprite.changeSpriteSheet(image, frameCount, columns, rows);
		if (type == BulletType.LASER) {
			sprite.setFitWidth(160);
			sprite.setFitHeight(40);
			sprite.setPreserveRatio(true);
			sprite.setOpacity(0.9);
			applyLaserEffects();
		} else {
			sprite.setFitWidth(36);
			sprite.setFitHeight(36);
		}
	}
	
	public void setBulletSprite(Image image) {
		// ใช้สำหรับภาพแบบ single frame
		setBulletSprite(image, 1, 1, 1);
	}

	public int getDamage() {
		return damage;
	}

	public BulletType getType() {
		return type;
	}

	public boolean consumeHit() {
		remainingHits = Math.max(0, remainingHits - 1);
		return remainingHits == 0;
	}
}
