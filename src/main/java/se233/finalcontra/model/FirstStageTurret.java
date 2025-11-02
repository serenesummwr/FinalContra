package se233.finalcontra.model;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Boss.Boss;
import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.EnemyState;
import se233.finalcontra.model.Enums.EnemyType;
import se233.finalcontra.view.GameStages.GameStage;

/**
 * Stationary turret guarding the stage four boss door.
 * Shoots at the player periodically and must be destroyed before the gate can be damaged.
 */
public class FirstStageTurret extends Enemy {
    private static final int DEFAULT_HEALTH = 1500;
    private static final int SHOOT_INTERVAL_FRAMES = 140;
    private static final int SHOOT_VISIBILITY_FRAMES = 18;
    private static final double BULLET_SPEED = 9.0;

    private final Rectangle hitboxOutline;
    private final ImageView turretOverlay;
    private final int normalWidth;
    private final int normalHeight;
    private final int brokenWidth;
    private final int brokenHeight;
    private int shootCooldown = SHOOT_INTERVAL_FRAMES;
    private int shootVisibilityTimer = 0;
    private boolean destroyed = false;

    public FirstStageTurret(int xPos, int yPos, int width, int height, int brokenWidth, int brokenHeight) {
        super(xPos, yPos, 0, width, height, width, height, 1, 1, 1,
                ImageAssets.BOSS1_TURRET_SHOOT, DEFAULT_HEALTH, EnemyType.BOSS4_TURRET);

        this.normalWidth = width;
        this.normalHeight = height;
        this.brokenWidth = brokenWidth;
        this.brokenHeight = brokenHeight;

        getSprite().setVisible(false);
        setMouseTransparent(true);

        turretOverlay = new ImageView(ImageAssets.BOSS1_TURRET_SHOOT);
        turretOverlay.setFitWidth(normalWidth);
        turretOverlay.setFitHeight(normalHeight);
        turretOverlay.setVisible(false);
        turretOverlay.setMouseTransparent(true);

        hitboxOutline = new Rectangle(width, height);
        hitboxOutline.setStroke(Color.RED);
        hitboxOutline.setStrokeWidth(2);
        hitboxOutline.setFill(Color.TRANSPARENT);
        hitboxOutline.setMouseTransparent(true);

        getChildren().addAll(turretOverlay, hitboxOutline);
        setState(EnemyState.IDLE);
    }

    @Override
    public void updateWithPlayer(Player player, GameStage gameStage) {
        if (!alive) {
            if (!destroyed) {
                showBrokenState();
            }
            return;
        }

        if (GameLoop.isPaused) {
            return;
        }

        if (shootCooldown > 0) {
            shootCooldown--;
            setState(EnemyState.IDLE);
        } else {
            fireAtPlayer(player, gameStage);
            shootCooldown = SHOOT_INTERVAL_FRAMES;
        }

        if (shootVisibilityTimer > 0) {
            shootVisibilityTimer--;
            if (shootVisibilityTimer <= 0 && alive) {
                turretOverlay.setVisible(false);
            }
        }
    }

    private void fireAtPlayer(Player player, GameStage gameStage) {
        setState(EnemyState.ATTACKING);

        Vector2D turretCenter = new Vector2D(
                getTranslateX() + width / 2.0,
                getTranslateY() + height / 2.0
        );
        Vector2D playerCenter = new Vector2D(
                player.getxPos() + Player.width / 2.0,
                player.getyPos() + Player.height / 2.0
        );

        Vector2D direction = playerCenter.subtract(turretCenter);
        if (direction.getLength() < 1) {
            direction = new Vector2D(-1, 0);
        } else {
            direction = direction.normalize();
        }

        Vector2D forwardOffset = direction.multiply(normalWidth / 2.0);
        Vector2D perpendicular = new Vector2D(-direction.y, direction.x).normalize();
        if (perpendicular.getLength() == 0) {
            perpendicular = new Vector2D(0, -1);
        }
        double barrelSpacing = Math.max(6, normalHeight * 0.25);
        Vector2D upperOffset = perpendicular.multiply(barrelSpacing);
        Vector2D lowerOffset = perpendicular.multiply(-barrelSpacing);

        Vector2D upperMuzzle = turretCenter.add(forwardOffset).add(upperOffset);
        Vector2D lowerMuzzle = turretCenter.add(forwardOffset).add(lowerOffset);

        Bullet upperBullet = new Bullet(upperMuzzle, direction, BULLET_SPEED, BulletOwner.ENEMY);
        Bullet lowerBullet = new Bullet(lowerMuzzle, direction, BULLET_SPEED, BulletOwner.ENEMY);

        SoundController.getInstance().playCannonSound();
        GameLoop.bullets.add(upperBullet);
        GameLoop.bullets.add(lowerBullet);
        Platform.runLater(() -> {
            gameStage.getChildren().add(upperBullet);
            gameStage.getChildren().add(lowerBullet);
        });

        turretOverlay.setImage(ImageAssets.BOSS1_TURRET_SHOOT);
        turretOverlay.setFitWidth(normalWidth);
        turretOverlay.setFitHeight(normalHeight);
        turretOverlay.setVisible(true);
        turretOverlay.toFront();
        hitboxOutline.toFront();
        shootVisibilityTimer = SHOOT_VISIBILITY_FRAMES;
    }

    private void showBrokenState() {
        destroyed = true;
        setState(EnemyState.DEAD);
        turretOverlay.setImage(ImageAssets.BOSS1_TURRET_BROKEN);
        turretOverlay.setFitWidth(brokenWidth);
        turretOverlay.setFitHeight(brokenHeight);
        turretOverlay.setVisible(true);
        turretOverlay.toFront();
        hitboxOutline.setVisible(false);
        shootVisibilityTimer = 0;
    }

    @Override
    public void takeDamage(int damage, Boss boss) {
        if (!alive) {
            return;
        }

        if (GameStage.totalMinions > 0) {
            SoundController.getInstance().playBlockHitSound();
            return;
        }

        SoundController.getInstance().playBlockHitSound();
        health -= damage;

        if (health <= 0) {
            health = 0;
            kill();
            GameLoop.addScore(500);
            SoundController.getInstance().playExplosionSound();
            showBrokenState();
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void resetOutlineVisibility(boolean visible) {
        if (destroyed) {
            hitboxOutline.setVisible(false);
        } else {
            hitboxOutline.setVisible(visible);
            if (visible) {
                hitboxOutline.toFront();
            }
        }
    }
}
