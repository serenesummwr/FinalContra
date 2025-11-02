package se233.finalcontra.model.Boss;

import java.util.Random;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.finalcontra.controller.GameLoop;
import se233.finalcontra.controller.SoundController;
import se233.finalcontra.model.Bullet;
import se233.finalcontra.model.Enums.BulletOwner;
import se233.finalcontra.model.Enums.BulletType;
import se233.finalcontra.model.Player;
import se233.finalcontra.model.Vector2D;
import se233.finalcontra.view.GameStages.GameStage;

public class ThirdBoss extends Boss {
    private static final int DEFAULT_MAX_HEALTH = 25000;
    private static final double TARGETED_BULLET_SPEED = 9.0;
    private static final double RING_BULLET_SPEED = 9.0;
    private static final double RAIN_BULLET_SPEED = 6.5;

    private final Rectangle hitbox;
    private final Runnable onDefeatedCallback;
    private final GameStage stage;
    private final Random random = new Random();

    private boolean defeatNotified;
    private boolean outlineVisible;

    private AttackPattern activePattern;
    private int attackCooldown = 100;
    private int patternCooldown;
    private int burstsRemaining;
    private int wavesRemaining;
    private boolean ringLaunched;

    public ThirdBoss(int xPos, int yPos, int width, int height, GameStage stage, Runnable onDefeatedCallback) {
        super(xPos, yPos, width, height, DEFAULT_MAX_HEALTH);
        this.stage = stage;
        this.onDefeatedCallback = onDefeatedCallback;

        hitbox = new Rectangle(width, height);
        hitbox.setFill(Color.color(1, 0, 0, 0.15));
        hitbox.setStroke(Color.RED);
        hitbox.setStrokeWidth(3);
        hitbox.setMouseTransparent(true);
        hitbox.setVisible(false);
        getChildren().add(hitbox);

        setMouseTransparent(true);
        setState(BossState.ATTACKING);
    }

    public Bounds getHitboxBounds() {
        return localToParent(hitbox.getBoundsInParent());
    }

    public void applyDamage(int amount) {
        if (getCurrentState() == BossState.DEFEATED) {
            return;
        }
        takeDamage(amount);
        if (getHealth() <= 0) {
            setState(BossState.DEFEATED);
            notifyDefeat();
        }
    }

    public boolean isDefeated() {
        return getCurrentState() == BossState.DEFEATED;
    }

    @Override
    public void update() {
        if (GameLoop.isPaused || getCurrentState() == BossState.DEFEATED) {
            return;
        }

        if (activePattern == null) {
            if (attackCooldown > 0) {
                attackCooldown--;
            } else {
                beginNextPattern();
            }
            return;
        }

        switch (activePattern) {
            case TARGETED_BURST -> runTargetedBurst();
            case SPREAD_RING -> runSpreadRing();
            case FALLING_RAIN -> runFallingRain();
        }
    }

    public void toggleHitboxOutline() {
        outlineVisible = !outlineVisible;
        hitbox.setVisible(outlineVisible);
        if (outlineVisible) {
            hitbox.toFront();
        }
    }

    public boolean isHitboxOutlineVisible() {
        return outlineVisible;
    }

    private void runTargetedBurst() {
        if (patternCooldown > 0) {
            patternCooldown--;
            return;
        }

        fireTargetedBurst();
        burstsRemaining--;
        patternCooldown = 20;
        if (burstsRemaining <= 0) {
            endCurrentPattern();
        }
    }

    private void runSpreadRing() {
        if (!ringLaunched) {
            fireRadialRing();
            ringLaunched = true;
            patternCooldown = 70;
            return;
        }
        if (patternCooldown > 0) {
            patternCooldown--;
        } else {
            endCurrentPattern();
        }
    }

    private void runFallingRain() {
        if (patternCooldown > 0) {
            patternCooldown--;
            return;
        }

        fireRainVolley();
        wavesRemaining--;
        patternCooldown = 12;
        if (wavesRemaining <= 0) {
            endCurrentPattern();
        }
    }

    private void beginNextPattern() {
        AttackPattern[] choices = AttackPattern.values();
        activePattern = choices[random.nextInt(choices.length)];
        switch (activePattern) {
            case TARGETED_BURST -> {
                burstsRemaining = 4;
                patternCooldown = 10;
            }
            case SPREAD_RING -> {
                ringLaunched = false;
                patternCooldown = 0;
            }
            case FALLING_RAIN -> {
                wavesRemaining = 6;
                patternCooldown = 8;
            }
        }
    }

    private void endCurrentPattern() {
        activePattern = null;
        ringLaunched = false;
        burstsRemaining = 0;
        wavesRemaining = 0;
        attackCooldown = 100 + random.nextInt(70);
    }

    private void fireTargetedBurst() {
        Player player = stage != null ? stage.getPlayer() : null;
        Vector2D origin = getMuzzlePosition();
        Vector2D direction = aimAtPlayer(origin, player);
        double[] offsets = {-18.0, 0.0, 18.0};
        for (double offset : offsets) {
            Vector2D rotated = rotate(direction, Math.toRadians(offset));
            spawnBossBullet(origin, rotated, TARGETED_BULLET_SPEED);
        }
        SoundController.getInstance().playCannonSound();
    }

    private void fireRadialRing() {
        Vector2D origin = getMuzzlePosition();
        int projectileCount = 14;
        for (int i = 0; i < projectileCount; i++) {
            double angle = (2 * Math.PI / projectileCount) * i;
            Vector2D dir = new Vector2D(Math.cos(angle), Math.sin(angle));
            spawnBossBullet(origin, dir, RING_BULLET_SPEED);
        }
        SoundController.getInstance().playLaserSound();
    }

    private void fireRainVolley() {
        Player player = stage != null ? stage.getPlayer() : null;
        Vector2D origin = getMuzzlePosition();
        double baseX = player != null ? player.getxPos() + Player.width / 2.0 : origin.getX();
        double spawnY = Math.max(30, getTranslateY() - getheight() * 0.2);

        for (int i = -2; i <= 2; i++) {
            double offset = i * 80 + randomRange(-30, 30);
            double spawnX = clamp(baseX + offset, 60, GameStage.WIDTH - 60);
            Vector2D spawn = new Vector2D(spawnX, spawnY);
            Vector2D direction = player != null
                    ? new Vector2D(player.getxPos() + Player.width / 2.0 - spawnX,
                    player.getyPos() + Player.height / 2.0 - spawnY)
                    : new Vector2D(0, 1);
            direction = direction.add(new Vector2D(randomRange(-0.2, 0.2), 0));
            spawnBossBullet(spawn, direction, RAIN_BULLET_SPEED);
        }
        SoundController.getInstance().playCannonSound();
    }

    private void spawnBossBullet(Vector2D origin, Vector2D direction, double speed) {
        if (stage == null) {
            return;
        }
        Vector2D normalized = normalize(direction);
        Bullet bullet = new Bullet(origin, normalized, speed, BulletOwner.ENEMY, BulletType.BOSS3);
        GameLoop.bullets.add(bullet);
        Platform.runLater(() -> {
            if (!stage.getChildren().contains(bullet)) {
                stage.getChildren().add(bullet);
            }
        });
    }

    private Vector2D aimAtPlayer(Vector2D origin, Player player) {
        if (player == null) {
            return new Vector2D(-1, 0);
        }
        Vector2D playerCenter = new Vector2D(
                player.getxPos() + Player.width / 2.0,
                player.getyPos() + Player.height / 2.0
        );
        return normalize(playerCenter.subtract(origin));
    }

    private Vector2D getMuzzlePosition() {
        double x = getTranslateX() + getwidth() * 0.5;
        double y = getTranslateY() + getheight() * 0.35;
        return new Vector2D(x, y);
    }

    private Vector2D rotate(Vector2D vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector2D(
                vector.getX() * cos - vector.getY() * sin,
                vector.getX() * sin + vector.getY() * cos
        );
    }

    private Vector2D normalize(Vector2D vector) {
        Vector2D normalized = vector != null ? vector.normalize() : new Vector2D(0, 0);
        if (normalized.getLength() == 0) {
            return new Vector2D(-1, 0);
        }
        return normalized;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double randomRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private void notifyDefeat() {
        if (defeatNotified || onDefeatedCallback == null) {
            return;
        }
        defeatNotified = true;
        activePattern = null;
        Platform.runLater(onDefeatedCallback);
    }

    private enum AttackPattern {
        TARGETED_BURST,
        SPREAD_RING,
        FALLING_RAIN
    }
}
